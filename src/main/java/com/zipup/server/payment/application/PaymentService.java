package com.zipup.server.payment.application;

import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.UUID;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentService {

  @Value("${toss.widget.secretKey}")
  private String secretKey;
  @Value("${toss.widget.api}")
  private String tossApi;

  private final RedisTemplate<String, String> redisTemplate;
  private final PaymentRepository paymentRepository;

  private void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 UUID입니다: " + id);
    }
  }

  @Transactional(readOnly = true)
  public Payment findById(String id) {
    isValidUUID(id);
    return paymentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new NoResultException("존재하지 않는 결제 내역이에요."));
  }

  public Boolean isOrderIdExist(String orderId) {
    return redisTemplate.opsForValue().get(orderId) == null;
  }

  public Boolean checkPaymentInfo(String orderId, Integer amount) {
    if (!isOrderIdExist(orderId)) return false;

    redisTemplate.opsForValue().set(orderId, String.valueOf(amount));
    return true;
  }

  public PaymentResultResponse successPayment(PaymentRequest request) throws IOException, ParseException {
    byte[] encodedBytes = Base64.getEncoder()
            .encode((secretKey + ":").getBytes(StandardCharsets.UTF_8));

    HttpURLConnection connection = postPaymentToToss(encodedBytes);

    OutputStream outputStream = connection.getOutputStream();

    JSONObject obj = new JSONObject();
    obj.put("orderId", request.getOrderId());
    obj.put("amount", request.getAmount());
    obj.put("paymentKey", request.getPaymentKey());

    outputStream.write(obj.toString().getBytes("UTF-8"));

    int code = connection.getResponseCode();
    boolean isSuccess = code == 200;

    InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

    Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(reader);

    System.out.println(jsonObject);
    String method = jsonObject.get("method").toString();
    String cardNumber = "";
    String accountNumber = "";
    String bank = "";
    String customerMobilePhone = "";
    String message = "";

    if (method != null) {
      if (method.equals("카드")) {
        cardNumber = ((JSONObject) jsonObject.get("card")).get("number").toString();
      } else if (method.equals("가상계좌")) {
        accountNumber = ((JSONObject) jsonObject.get("virtualAccount")).get("accountNumber").toString();
      } else if (method.equals("계좌이체")) {
        bank = ((JSONObject) jsonObject.get("transfer")).get("bank").toString();
      } else if (method.equals("휴대폰")) {
        customerMobilePhone = ((JSONObject) jsonObject.get("mobilePhone")).get("customerMobilePhone").toString();
      }

      Payment successPayment = Payment.builder()
              .orderId(request.getOrderId())
              .paymentKey(request.getPaymentKey())
              .price(request.getAmount())
              .bank(bank)
              .paymentMethod(method)
              .build();

      paymentRepository.save(successPayment);
      redisTemplate.delete(request.getOrderId());
    } else {
      code = Integer.parseInt(jsonObject.get("code").toString());
      message = jsonObject.get("message").toString();
    }

    return PaymentResultResponse.builder()
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

  public HttpURLConnection postPaymentToToss(byte[] encodedBytes) throws IOException {
    String authorizations = "Basic " + new String(encodedBytes);

    URL url = new URL(tossApi);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authorizations);
    connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    connection.setRequestMethod(String.valueOf(HttpMethod.POST));
    connection.setDoOutput(true);

    return connection;
  }

}
