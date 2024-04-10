package com.zipup.server.payment.presentation;

import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.dto.PaymentRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {

  private final PaymentService paymentService;

  @Operation(summary = "결제 정보 저장", description = "클라이언트에서 결제 요청하기 전에 결제 정보를 서버에 저장")
  @Parameter(name = "orderId", description = "주문 번호")
  @Parameter(name = "amount", description = "결제 금액")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "저장 성공",
                  content = @Content(schema = @Schema(type = "결제 진행!"))),
          @ApiResponse(
                  responseCode = "409",
                  description = "동일한 주문 번호가 존재하는 경우",
                  content = @Content(schema = @Schema(type = "동일한 주문 번호가 존재해요.")))
  })
  @PostMapping("/")
  public ResponseEntity<String> checkPaymentInfo(@RequestParam(value = "orderId") String orderId,
                                                  @RequestParam(value = "amount") Integer amount) {
    return paymentService.checkPaymentInfo(orderId, amount)
            ? ResponseEntity.ok("결제 진행!")
            : ResponseEntity.status(HttpStatus.CONFLICT).body("동일한 주문 번호가 존재해요.");
  }

  @Operation(summary = "결제 실패", description = "결제 실패")
  @Parameter(name = "message", description = "결제 실패")
  @Parameter(name = "code", description = "400")
  @ApiResponse(
          responseCode = "400",
          description = "결제 실패",
          content = @Content(schema = @Schema(type = "Payment failed")))
  @GetMapping(value = "/fail")
  public ResponseEntity<String> failPayment(
          @RequestParam(value = "message") String message,
          @RequestParam(value = "code") Integer code
  ) {
    log.error("message : {}\ncode : {} " + message, code);
    return ResponseEntity.status(code).body(message);
  }

  @Operation(summary = "결제 성공 여부", description = "서버에서 토스페이먼츠로 결제 승인 요청")
  @Parameter(name = "orderId", description = "주문 번호")
  @Parameter(name = "amount", description = "결제 금액")
  @Parameter(name = "paymentKey", description = "결제 키")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "저장 성공",
                  content = @Content(schema = @Schema(implementation = PaymentResultResponse.class))),
          @ApiResponse(
                  responseCode = "401",
                  description = "키 오류",
                  content = @Content(schema = @Schema(implementation = PaymentResultResponse.class))),
          @ApiResponse(
                  responseCode = "404",
                  description = "결제 시간 만료",
                  content = @Content(schema = @Schema(implementation = PaymentResultResponse.class))),
  })
  @GetMapping(value = "/confirm")
  public ResponseEntity<PaymentResultResponse> successPayment(
          @RequestParam(value = "orderId") String orderId,
          @RequestParam(value = "amount") Integer amount,
          @RequestParam(value = "paymentKey") String paymentKey) throws Exception {

    PaymentResultResponse response = paymentService.successPayment(new PaymentRequest(orderId, amount, paymentKey));

    return ResponseEntity.status(response.getCode()).body(response);
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<PaymentResultResponse>> getPaymentList() {
    List<PaymentResultResponse> response = paymentService.getPaymentList();
    return ResponseEntity.ok(response);
  }

}
