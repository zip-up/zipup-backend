package com.zipup.server.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.global.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class TossService {

  @Value("${toss.widget.secretKey}")
  private String SECRET_KEY;
  @Value("${toss.widget.api.payments}")
  private String TOSS_API;
  private WebClient webClient;

  @PostConstruct
  public void init() {
    byte[] encodedBytes = Base64.getEncoder()
            .encode((SECRET_KEY + ":").getBytes(UTF_8));
    String authorizations = "Basic " + new String(encodedBytes);

    webClient = WebClient.builder()
            .baseUrl(TOSS_API)
            .defaultHeaders(header -> {
                    header.set(AUTHORIZATION, authorizations);
                    header.setContentType(APPLICATION_JSON);
            })
            .build();
  }

  public <T> Mono<T> get(String uri, Class<T> responseDtoClass) {
    return webClient.get()
            .uri(uri)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse ->
              clientResponse.bodyToMono(ErrorResponse.class).flatMap(error ->
                      Mono.error(new PaymentException(clientResponse.statusCode().value(), error.getError_name(), error.getMessage())))
            )
            .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                    clientResponse.bodyToMono(ErrorResponse.class).flatMap(error ->
                                    Mono.error(new PaymentException(clientResponse.statusCode().value(), error.getError_name(), error.getMessage())))
            )
            .bodyToMono(responseDtoClass);
  }

  public <T> Mono<T> post(String uri, Map<String, Object> request, Class<T> responseDtoClass) {
    Optional<String> idempotencyKey = Optional.ofNullable((String) request.get("idempotencyKey"));
    request.remove("idempotencyKey");

    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody;
    try {
      requestBody = objectMapper.writeValueAsString(request);
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }

    WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
            .uri(uri)
            .body(BodyInserters.fromValue(requestBody));

    idempotencyKey.ifPresent(key -> {
      if (!key.isEmpty()) requestSpec.header("Idempotency-Key", key);
    });

    return requestSpec.retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse ->
                    clientResponse.bodyToMono(ErrorResponse.class).flatMap(error ->
                            Mono.error(new PaymentException(clientResponse.statusCode().value(), error.getError_name(), error.getMessage())))
            )
            .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                    clientResponse.bodyToMono(ErrorResponse.class).flatMap(error ->
                            Mono.error(new PaymentException(clientResponse.statusCode().value(), error.getError_name(), error.getMessage())))
            )
            .bodyToMono(responseDtoClass);
  }

}
