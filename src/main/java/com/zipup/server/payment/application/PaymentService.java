package com.zipup.server.payment.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.PaymentException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.exception.UniqueConstraintException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.payment.dto.PaymentConfirmRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
import com.zipup.server.payment.dto.TossPaymentResponse;
import com.zipup.server.payment.infrastructure.PaymentRepository;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.infrastructure.PresentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;
import static com.zipup.server.global.util.entity.PaymentStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final TossService tossService;
  private final RedisTemplate<String, String> redisTemplate;
  private final PaymentRepository paymentRepository;
  private final PresentRepository presentRepository;

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

  public Boolean isOrderIdExistInRedis(String orderId, Integer amount, String userId) {
    return Objects.equals(redisTemplate.opsForValue().get(orderId + userId), String.valueOf(amount));
  }

  public void checkPaymentInfo(String orderId, Integer amount, String userId) {
    if (isOrderIdExistInRedis(orderId, amount, userId))
      throw new UniqueConstraintException("OrderId, UserId", orderId + " " + userId);
    redisTemplate.opsForValue().set(orderId + userId, String.valueOf(amount));
  }

  @Transactional
  public PaymentResultResponse confirmPayment(PaymentConfirmRequest request, String userId) {
    if (existsByPaymentKey(request.getPaymentKey())) throw new UniqueConstraintException("PaymentKey", request.getPaymentKey());
    if (existsByOrderId(request.getOrderId())) throw new UniqueConstraintException("OrderId", request.getOrderId());
    if (!isOrderIdExistInRedis(request.getOrderId(), request.getAmount(), userId)) throw new ResourceNotFoundException(DATA_NOT_FOUND);

    Map<String, Object> data = new HashMap<>();
    data.put("orderId", request.getOrderId());
    data.put("amount", request.getAmount());
    data.put("paymentKey", request.getPaymentKey());

    Mono<TossPaymentResponse> resultResponseMono = tossService.post("/confirm", data, TossPaymentResponse.class);
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
    redisTemplate.delete(request.getOrderId() + userId);

    return paymentResult.toDetailResponse();
  }

  @Transactional(readOnly = true)
  public List<PaymentResultResponse> getPaymentList() {
    return paymentRepository.findAll()
            .stream()
            .map(Payment::toDetailResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public void updatePaymentStatus() {
    getPaymentList().stream()
            .filter(payment -> !payment.getStatus().startsWith(INVALID_PAYMENT_STATUS.name()))
            .forEach(payment -> fetchPaymentByPaymentKey(payment.getPaymentKey()).onErrorResume(throwable -> {
              if (throwable instanceof PaymentException) {
                log.info("Id :: {}", payment.getId());
                log.info("Status :: {}", payment.getStatus());
                Payment targetPayment = findByPaymentKey(payment.getPaymentKey());
                changeInvalidPaymentStatus(targetPayment);
                log.info("Change Status :: {}", payment.getStatus());
              }
              return Mono.empty();
            }).subscribe());
  }

  @Transactional
  public Mono<TossPaymentResponse> fetchPaymentByPaymentKey(String paymentKey) {
    return tossService.get("/" + paymentKey, TossPaymentResponse.class)
            .flatMap(tossPaymentResponse -> {
              if (tossPaymentResponse != null) {
                Payment payment = findByPaymentKey(tossPaymentResponse.getPaymentKey());
                payment.setPaymentStatus(tossPaymentResponse.getStatus());
              }
              return Mono.justOrEmpty(tossPaymentResponse);
            });
  }

  @Transactional
  public Mono<TossPaymentResponse> fetchPaymentByOrderId(String orderId) {
    return tossService.get("/orders/" + orderId, TossPaymentResponse.class)
            .flatMap(tossPaymentResponse -> {
              if (tossPaymentResponse != null) {
                Payment payment = findByPaymentKey(tossPaymentResponse.getPaymentKey());
                payment.setPaymentStatus(tossPaymentResponse.getStatus());
              }
              return Mono.justOrEmpty(tossPaymentResponse);
            });
  }

  @Transactional
  public PaymentResultResponse cancelPayment(PaymentCancelRequest request) {
    Payment payment = findByPaymentKey(request.getPaymentKey());
    String idempotencyKey = payment.getId().toString();
    Present targetPresent = presentRepository.findByPayment(payment)
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));

    if (!targetPresent.getUser()
            .getId().toString()
            .equals(request.getUserId()))
      throw new BaseException(ACCESS_DENIED);

    if (payment.getPaymentStatus().equals(INVALID_PAYMENT_STATUS)) {
      changeCancelInvalidPaymentStatus(payment);
      return payment.toCancelResponse(null);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("idempotencyKey", idempotencyKey);
    data.put("cancelReason", request.getCancelReason());
    if (request.getCancelAmount() != null)
      data.put("cancelAmount", request.getCancelAmount());
    if (request.getRefundReceiveAccount() != null) {
      data.put("refundReceiveAccount", request.getRefundReceiveAccount());
    }

    Mono<TossPaymentResponse> response = tossService.post("/" + request.getPaymentKey() + "/cancel", data, TossPaymentResponse.class);
    if (request.getCancelAmount() == null || request.getCancelAmount().equals(payment.getBalanceAmount())) {
      changeCancelPaymentStatus(payment);
    } else {
      if (request.getCancelAmount() < payment.getBalanceAmount()) {
        payment.setBalanceAmount(payment.getBalanceAmount() - request.getCancelAmount());
        changePartialCancelPaymentStatus(payment);
      }
    }

    return Optional.ofNullable(response.block())
            .map(TossPaymentResponse::getCancels)
            .map(payment::toCancelResponse)
            .orElseThrow(() -> new BaseException(UNKNOWN_ERROR));
  }

  @Transactional
  public void changePrivatePayment(Payment payment) {
    payment.setStatus(ColumnStatus.PRIVATE);
  }

  @Transactional
  public void changeUnlinkPayment(Payment payment) {
    payment.setStatus(ColumnStatus.UNLINK);
  }

  @Transactional
  public void changeCancelPaymentStatus(Payment payment) {
    payment.setPaymentStatus(CANCELED);
  }

  @Transactional
  public void changePartialCancelPaymentStatus(Payment payment) {
    payment.setPaymentStatus(PARTIAL_CANCELED);
  }

  @Transactional
  public void changeCancelInvalidPaymentStatus(Payment payment) {
    payment.setPaymentStatus(INVALID_PAYMENT_STATUS_CANCELED);
  }

  @Transactional
  public void changeInvalidPaymentStatus(Payment payment) {
    payment.setPaymentStatus(INVALID_PAYMENT_STATUS);
  }

}
