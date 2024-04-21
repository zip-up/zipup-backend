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
public class RefundReceiveAccount {
  @Schema(description = "환불받을 계좌의 은행 코드")
  private String bankCode;
  @Schema(description = "환불받을 계좌번호")
  private String accountNumber;
  @Schema(description = "환불받을 계좌의 예금주")
  private String holderName;
}
