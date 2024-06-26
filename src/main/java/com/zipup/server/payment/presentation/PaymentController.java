package com.zipup.server.payment.presentation;

import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.payment.dto.PaymentConfirmRequest;
import com.zipup.server.payment.dto.PaymentResultResponse;
import com.zipup.server.payment.dto.TossPaymentResponse;
import io.swagger.v3.oas.annotations.Hidden;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
                  description = "저장 성공"),
          @ApiResponse(
                  responseCode = "409",
                  description = "동일한 주문 번호가 존재하는 경우",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/")
  public void checkPaymentInfo(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          final @RequestParam(value = "orderId") String orderId,
          final @RequestParam(value = "amount") Integer amount
  ) {
    paymentService.checkPaymentInfo(orderId, amount, user.getUsername());
  }

  @Operation(summary = "결제 승인", description = "서버에서 토스페이먼츠로 결제 승인 요청")
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
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "404",
                  description = "결제 시간 만료",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/confirm")
  public PaymentResultResponse successPayment(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          final @RequestParam(value = "orderId") String orderId,
          final @RequestParam(value = "amount") Integer amount,
          final @RequestParam(value = "paymentKey") String paymentKey
  ) {
    return paymentService.confirmPayment(new PaymentConfirmRequest(orderId, amount, paymentKey), user.getUsername());
  }

  @Operation(summary = "결제 취소", description = "paymentKey 로 결제 취소")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "승인된 결제를 paymentKey 로 취소합니다. 취소 이유를 cancelReason 에 추가해야 합니다.\n" +
          "결제 금액의 일부만 부분 취소하려면 cancelAmount 에 취소할 금액을 추가해서 API 요청합니다. cancelAmount 에 값을 넣지 않으면 전액 취소됩니다.")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "결제 취소에 성공하면 TossPaymentResponse 객체의 cancels 키에 취소 기록을 배열로 드립니다.\n각 취소 거래마다 거래를 구분하는 transactionKey 를 가지고 있습니다.",
                  content = @Content(schema = @Schema(implementation = TossPaymentResponse.class))),
          @ApiResponse(
                  responseCode = "401",
                  description = "인증되지 않은 시크릿 키 혹은 클라이언트 키",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "403",
                  description = "반복적인 요청은 허용되지 않음",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "404",
                  description = "존재하지 않는 결제 정보",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/cancel")
  @Hidden
  public PaymentResultResponse cancelPayment(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestBody PaymentCancelRequest request
  ) {
    request.setUserId(user.getUsername());

    return paymentService.cancelPayment(request);
  }

  @Operation(summary = "paymentKey 로 결제 조회", description = "paymentKey 로 결제 조회")
  @Parameter(name = "paymentKey", description = "결제 키")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "조회 성공",
                  content = @Content(schema = @Schema(implementation = TossPaymentResponse.class))),
          @ApiResponse(
                  responseCode = "401",
                  description = "인증되지 않은 시크릿 키 혹은 클라이언트 키",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "403",
                  description = "반복적인 요청은 허용되지 않음",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "404",
                  description = "존재하지 않는 결제 정보",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/key")
  public TossPaymentResponse getPaymentByPaymentKey(
          final @RequestParam(value = "paymentKey") String paymentKey
  ) {
    return paymentService.fetchPaymentByPaymentKey(paymentKey).block();
  }

  @Operation(summary = "orderId로 결제 조회", description = "orderId로 결제 조회")
  @Parameter(name = "orderId", description = "주문 정보 키")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "저장 성공",
                  content = @Content(schema = @Schema(implementation = TossPaymentResponse.class))),
          @ApiResponse(
                  responseCode = "401",
                  description = "인증되지 않은 시크릿 키 혹은 클라이언트 키",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "403",
                  description = "반복적인 요청은 허용되지 않음",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
                  responseCode = "404",
                  description = "존재하지 않는 결제 정보",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/order")
  public TossPaymentResponse getPaymentByOrderId(
          final @RequestParam(value = "orderId") String orderId
  ) {
    return paymentService.fetchPaymentByOrderId(orderId).block();
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<PaymentResultResponse>> getPaymentList() {
    List<PaymentResultResponse> response = paymentService.getPaymentList();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "결제 데이터 업데이트", description = "결제 데이터 업데이트")
  @PutMapping("/setting")
  public ResponseEntity<Void> updatePaymentStatus() {
    paymentService.updatePaymentStatus();
    return ResponseEntity.ok().build();
  }

}
