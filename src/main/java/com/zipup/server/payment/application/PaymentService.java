package com.zipup.server.payment.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.PaymentException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.payment.dto.PaymentConfirmRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
import com.zipup.server.payment.dto.TossPaymentResponse;
import com.zipup.server.payment.infrastructure.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class PaymentService {

  @Value("${toss.widget.secretKey}")
  private String SECRET_KEY;
  @Value("${toss.widget.api.payments}")
  private String TOSS_PAYMENTS_API;

  private final TossService tossService;
  private final RedisTemplate<String, String> redisTemplate;
  private final PaymentRepository paymentRepository;

  @Transactional(readOnly = true)
  public Payment findById(String id) {
    isValidUUID(id);
    return paymentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  public Boolean isOrderIdExist(String orderId) {
    return redisTemplate.opsForValue().get(orderId) == null;
  }

  public Boolean checkPaymentInfo(String orderId, Integer amount) {
    if (!isOrderIdExist(orderId)) return false;

    redisTemplate.opsForValue().set(orderId, String.valueOf(amount));
    return true;
  }

  public PaymentResultResponse successPayment(PaymentConfirmRequest request) throws IOException, ParseException {
    byte[] encodedBytes = Base64.getEncoder()
            .encode((SECRET_KEY + ":").getBytes(UTF_8));

    HttpURLConnection connection = confirmPaymentToToss(encodedBytes);

    OutputStream outputStream = connection.getOutputStream();

    Map<String, Object> data = new HashMap<>();
    data.put("orderId", request.getOrderId());
    data.put("amount", request.getAmount());
    data.put("paymentKey", request.getPaymentKey());

    JSONObject obj = new JSONObject(data);

    outputStream.write(obj.toString().getBytes(UTF_8));

    int code = connection.getResponseCode();
    boolean isSuccess = code == 200;

    InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

    Reader reader = new InputStreamReader(responseStream, UTF_8);
    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(reader);

    if (!isSuccess)
      throw new PaymentException(code, jsonObject.get("code").toString(), jsonObject.get("message").toString());

    System.out.println(jsonObject);
    String method = jsonObject.get("method").toString();
    String cardNumber = "";
    String accountNumber = "";
    String bank = "";
    String customerMobilePhone = "";
    String message = "";
    String resultId = "";

    if (method != null) {
      switch (method) {
        case "카드":
          cardNumber = ((JSONObject) jsonObject.get("card")).get("number").toString();
          break;
        case "가상계좌":
          accountNumber = ((JSONObject) jsonObject.get("virtualAccount")).get("accountNumber").toString();
          break;
        case "계좌이체":
          bank = ((JSONObject) jsonObject.get("transfer")).get("bank").toString();
          break;
        case "휴대폰":
          customerMobilePhone = ((JSONObject) jsonObject.get("mobilePhone")).get("customerMobilePhone").toString();
          break;
      }

      Payment successPayment = Payment.builder()
              .orderId(request.getOrderId())
              .paymentKey(request.getPaymentKey())
              .price(request.getAmount())
              .bank(bank)
              .paymentMethod(method)
              .build();

      resultId = paymentRepository.save(successPayment).getId().toString();
      redisTemplate.delete(request.getOrderId());
    } else {
      code = Integer.parseInt(jsonObject.get("code").toString());
      message = jsonObject.get("message").toString();
    }
    responseStream.close();

    return PaymentResultResponse.builder()
            .id(resultId)
            .code(code)
            .method(method)
            .responseStr(jsonObject.toString())
            .cardNumber(cardNumber)
            .accountNumber(accountNumber)
            .bank(bank)
            .customerMobilePhone(customerMobilePhone)
            .message(message)
            .build();
  }

  public HttpURLConnection confirmPaymentToToss(byte[] encodedBytes) throws IOException {
    String authorizations = "Basic " + new String(encodedBytes);

    URL url = new URL(TOSS_PAYMENTS_API + "/confirm");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty(AUTHORIZATION, authorizations);
    connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    connection.setRequestMethod(String.valueOf(HttpMethod.POST));
    connection.setDoOutput(true);

    return connection;
  }

  @Transactional(readOnly = true)
  public List<PaymentResultResponse> getPaymentList() {
    return paymentRepository.findAll()
            .stream()
            .map(Payment::toDetailResponse)
            .collect(Collectors.toList());
  }

  public TossPaymentResponse fetchPaymentByPaymentKey(String paymentKey) {
    Mono<TossPaymentResponse> response = tossService.get("/" + paymentKey, TossPaymentResponse.class);
    return response.block();
  }

  public TossPaymentResponse fetchPaymentByOrderId(String orderId) {
    Mono<TossPaymentResponse> response = tossService.get("/orders/" + orderId, TossPaymentResponse.class);
    return response.block();
  }

  public TossPaymentResponse cancelPayment(PaymentCancelRequest request) {
    Payment payment = paymentRepository.findByPaymentKey(request.getPaymentKey())
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
    String idempotencyKey = payment.getId().toString();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!payment.getPresent().getUser().getId().toString().equals(authentication.getName()))
      throw new BaseException(ACCESS_DENIED);

    Map<String, Object> data = new HashMap<>();
    data.put("idempotencyKey", idempotencyKey);
    data.put("cancelReason", request.getCancelReason());
    if (request.getCancelAmount() != null)
      data.put("cancelAmount", request.getCancelAmount());
    if (request.getRefundReceiveAccount() != null) {
      data.put("refundReceiveAccount", request.getRefundReceiveAccount());
    }

    JSONObject obj = new JSONObject(data);
    Mono<TossPaymentResponse> response = tossService.post("/" + request.getPaymentKey() + "/cancel", obj, TossPaymentResponse.class);
    return response.block();
  }

}
