package com.zipup.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transfer {
  @Schema(description = "가상계좌 은행 숫자 코드")
  private String bankCode;
  @Schema(description = "정산 상태. 'INCOMPLETED', 'COMPLETED'")
  private String settlementStatus;
}
