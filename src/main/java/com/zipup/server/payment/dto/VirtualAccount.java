package com.zipup.server.payment.dto;

import com.zipup.server.global.util.entity.RefundStatus;
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
public class VirtualAccount {
  @Schema(description = "가상계좌 타입. '일반', '고정' 중 하나")
  private String accountType;
  @Schema(description = "발급된 계좌번호")
  private String accountNumber;
  @Schema(description = "가상계좌 은행 숫자 코드")
  private String bankCode;
  @Schema(description = "가상계좌를 발급한 구매자명")
  private String customerName;
  @Schema(description = "입금 기한")
  private OffsetDateTime dueDate;
  @Schema(description = "환불 처리 상태")
  private RefundStatus refundStatus;
  @Schema(description = "가상계좌의 만료 여부")
  private Boolean expired;
  @Schema(description = "환불 계좌 정보")
  private RefundReceiveAccount refundReceiveAccount;
}
