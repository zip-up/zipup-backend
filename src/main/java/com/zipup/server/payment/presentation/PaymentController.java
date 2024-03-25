package com.zipup.server.payment.presentation;

import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.dto.PaymentRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/")
  public ResponseEntity<String> checkPaymentInfo(@RequestParam(value = "orderId") String orderId,
                                                  @RequestParam(value = "amount") Integer amount) {
    return paymentService.checkPaymentInfo(orderId, amount)
            ? ResponseEntity.ok("결제 진행!")
            : ResponseEntity.status(HttpStatus.CONFLICT).body("동일한 주문 번호가 존재해요.");
  }

  @GetMapping(value = "/fail")
  public ResponseEntity<String> failPayment(
          @RequestParam(value = "message") String message,
          @RequestParam(value = "code") Integer code
  ) {
    log.error("message : {}\ncode : {} " + message, code);
    return ResponseEntity.status(code).body(message);
  }

  @GetMapping(value = "/success")
  public ResponseEntity<PaymentResultResponse> successPayment(
          @RequestParam(value = "orderId") String orderId,
          @RequestParam(value = "amount") Integer amount,
          @RequestParam(value = "paymentKey") String paymentKey) throws Exception {

    PaymentResultResponse response = paymentService.successPayment(new PaymentRequest(orderId, amount, paymentKey));

    return ResponseEntity.status(response.getCode()).body(response);
  }

}
