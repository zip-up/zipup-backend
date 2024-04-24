package com.zipup.server.payment.application;

import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.payment.dto.RefundReceiveAccount;
import com.zipup.server.payment.infrastructure.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PaymentServiceTest {
  @Autowired
  private PaymentService paymentService;
  @Mock
  private PaymentRepository paymentRepository;

  private Payment payment;
  private Payment savedEntity;
  private PaymentCancelRequest request;


  @BeforeEach
  void setUp() {
    payment = Payment.builder()
            .paymentKey("paymentKey")
            .paymentMethod("가상계좌")
            .price(45000)
            .bank("88")
            .build();
    savedEntity = paymentRepository.save(payment);

    RefundReceiveAccount account = RefundReceiveAccount.builder()
            .accountNumber("accountNumber")
            .bank("88")
            .holderName("김토스")
            .build();

    request = new PaymentCancelRequest(
            "paymentKey",
            "cancelReason",
            10000,
            account
    );
  }

  @Test
  void cancelPayment_PaymentNotFound_ThrowsResourceNotFoundException() {
    // given
    when(paymentRepository.findByPaymentKey("invalidKey")).thenReturn(Optional.empty());
    // when, then
    assertThrows(ResourceNotFoundException.class, () -> paymentService.cancelPayment(request));
  }

}
