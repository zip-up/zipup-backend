package com.zipup.server.payment.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.global.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.exception.CustomErrorCode.UNKNOWN_ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class TossService {

  @Value("${toss.widget.secretKey}")
  private String SECRET_KEY;
  private final WebClient webClient;

  public TossService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.baseUrl("https://api.tosspayments.com/v1/payments").build();
  }

  public <T> Mono<T> get(String uri, Class<T> responseDtoClass) {
    byte[] encodedBytes = Base64.getEncoder()
            .encode((SECRET_KEY + ":").getBytes(UTF_8));
    String authorizations = "Basic " + new String(encodedBytes);

    return webClient.get()
            .uri(uri)
            .header(AUTHORIZATION, authorizations)
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

}
