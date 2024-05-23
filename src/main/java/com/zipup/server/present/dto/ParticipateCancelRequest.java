package com.zipup.server.present.dto;

import com.zipup.server.payment.dto.RefundReceiveAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipateCancelRequest {
  @Schema(description = "결제 식별자 값")
  private String paymentId;
  @Schema(description = "취소 사유", required = true)
  private String cancelReason;
  @Schema(description = "부분 취소할 금액, 값이 없으면 전액 취소. 입금 전엔 전체 금액 취소만 가능")
  private Integer cancelAmount;
  @Schema(description = "결제 취소 후 환불될 계좌 정보. '가상계좌 결제만 필수', 다른 결제수단으로 이루어진 결제를 취소할 땐 Null로 주세요")
  private RefundReceiveAccount refundReceiveAccount;
}
