package com.zipup.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelRecord {
  @Schema(description = "결제 취소 금액")
  private Integer cancelAmount;
  @Schema(description = "결제 취소 이유")
  private String cancelReason;
  @Schema(description = "취소 건의 키 값")
  private String transactionKey;
  @Schema(description = "결제 취소가 일어난 날짜와 시간 정보")
  private OffsetDateTime canceledAt;
  @Schema(description = "취소 건의 현금영수증 키 값")
  private String receiptKey;
  @Schema(description = "취소 상태입니다. DONE이면 결제가 성공적으로 취소된 상태")
  private String cancelStatus;

}
