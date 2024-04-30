package com.zipup.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerMobilePhone {
  @Schema(description = "전체 휴대폰 번호")
  private String plain;
  @Schema(description = "중간 4자리가 마스킹 된 휴대폰 번호")
  private String masking;
}
