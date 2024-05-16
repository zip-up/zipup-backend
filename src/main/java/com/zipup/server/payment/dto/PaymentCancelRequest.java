package com.zipup.server.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentCancelRequest {
  @Schema(description = "유저 식별자")
  private String userId;
  @Schema(description = "결제 키 값 ", required = true)
  private String paymentKey;
  @Schema(description = "취소 사유", required = true)
  private String cancelReason;
  @Schema(description = "부분 취소할 금액, 값이 없으면 전액 취소. 입금 전엔 전체 금액 취소만 가능")
  private Integer cancelAmount;
  @Schema(description = "결제 취소 후 환불될 계좌 정보. '가상계좌 결제만 필수', 다른 결제수단으로 이루어진 결제를 취소할 땐 Null로 주세요")
  private RefundReceiveAccount refundReceiveAccount;
}
