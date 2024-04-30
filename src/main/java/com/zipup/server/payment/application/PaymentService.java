package com.zipup.server.payment.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.exception.UniqueConstraintException;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.payment.dto.PaymentConfirmRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
import com.zipup.server.payment.dto.TossPaymentResponse;
import com.zipup.server.payment.infrastructure.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;
import static com.zipup.server.global.util.entity.PaymentStatus.CANCELED;
import static com.zipup.server.global.util.entity.PaymentStatus.PARTIAL_CANCELED;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final TossService tossService;
  private final RedisTemplate<String, String> redisTemplate;
  private final PaymentRepository paymentRepository;

  @Transactional(readOnly = true)
  public Payment findById(String id) {
    isValidUUID(id);
    return paymentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Payment findByPaymentKey(String paymentKey) {
    return paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Boolean existsByPaymentKey(String paymentKey) {
    return paymentRepository.existsByPaymentKey(paymentKey);
  }

  @Transactional(readOnly = true)
  public Boolean existsByOrderId(String orderId) {
    return paymentRepository.existsByOrderId(orderId);
  }

  public Boolean isOrderIdExistInRedis(String orderId) {
    return redisTemplate.opsForValue().get(orderId) != null;
  }

  public Boolean checkPaymentInfo(String orderId, Integer amount) {
    if (isOrderIdExistInRedis(orderId)) return false;

    redisTemplate.opsForValue().set(orderId, String.valueOf(amount));
    return true;
  }

  @Transactional
  public PaymentResultResponse confirmPayment(PaymentConfirmRequest request) {
    if (existsByPaymentKey(request.getPaymentKey())) throw new UniqueConstraintException("PaymentKey", request.getPaymentKey());
    if (existsByOrderId(request.getOrderId())) throw new UniqueConstraintException("OrderId", request.getOrderId());
    if (!isOrderIdExistInRedis(request.getOrderId())) throw new ResourceNotFoundException(DATA_NOT_FOUND);

    Map<String, Object> data = new HashMap<>();
    data.put("orderId", request.getOrderId());
    data.put("amount", request.getAmount());
    data.put("paymentKey", request.getPaymentKey());
    JSONObject obj = new JSONObject(data);

    Mono<TossPaymentResponse> resultResponseMono = tossService.post("/confirm", obj, TossPaymentResponse.class);
    TossPaymentResponse response = resultResponseMono.block();
    String method = response != null ? response.getMethod() : null;

    if (method == null)
      throw new BaseException(UNKNOWN_ERROR);

    String cardNumber = "";
    String accountNumber = "";
    String bank = "";
    String phoneNumber = "";
    String easyPay = "";

    switch (method) {
      case "카드":
        cardNumber = response.getCard().getNumber();
        break;
      case "가상계좌":
        bank = response.getVirtualAccount().getBankCode();
        accountNumber = response.getVirtualAccount().getAccountNumber();
        break;
      case "계좌이체":
        bank = response.getTransfer().getBankCode();
        break;
      case "휴대폰":
        phoneNumber = response.getMobilePhone().getCustomerMobilePhone().getMasking();
        break;
      case "간편결제":
        easyPay = response.getEasyPay().getProvider();
        break;
    }

    Payment paymentResult = Payment.builder()
            .paymentKey(response.getPaymentKey())
            .orderId(response.getOrderId())
            .paymentMethod(method)
            .totalAmount(response.getTotalAmount())
            .balanceAmount(response.getTotalAmount())
            .paymentStatus(response.getStatus())
            .bank(bank)
            .cardNumber(cardNumber)
            .accountNumber(accountNumber)
            .phoneNumber(phoneNumber)
            .easyPay(easyPay)
            .build();

    paymentRepository.save(paymentResult);
    redisTemplate.delete(request.getOrderId());

    return paymentResult.toDetailResponse();
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

  @Transactional
  public PaymentResultResponse cancelPayment(PaymentCancelRequest request) {
    Payment payment = findByPaymentKey(request.getPaymentKey());
    String idempotencyKey = payment.getId().toString();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!payment.getPresent().getUser().getId().toString().equals(authentication.getName()))
      throw new BaseException(ACCESS_DENIED);

    Map<String, Object> data = new HashMap<>();
    data.put("idempotencyKey", idempotencyKey+"dd");
    data.put("cancelReason", request.getCancelReason());
    if (request.getCancelAmount() != null)
      data.put("cancelAmount", request.getCancelAmount());
    if (request.getRefundReceiveAccount() != null) {
      data.put("refundReceiveAccount", request.getRefundReceiveAccount());
    }

    JSONObject obj = new JSONObject(data);
    Mono<TossPaymentResponse> response = tossService.post("/" + request.getPaymentKey() + "/cancel", obj, TossPaymentResponse.class);
    if (request.getCancelAmount() == null || request.getCancelAmount().equals(payment.getBalanceAmount())) {
      payment.setPaymentStatus(CANCELED);
    } else {
      if (request.getCancelAmount() < payment.getBalanceAmount()) {
        payment.setBalanceAmount(payment.getBalanceAmount() - request.getCancelAmount());
        payment.setPaymentStatus(PARTIAL_CANCELED);
      }
    }

    return payment.toCancelResponse(response.block().getCancels());
  }

}
