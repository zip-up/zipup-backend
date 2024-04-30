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
public class EasyPay {
  @Schema(description = "간편결제사 코드v")
  private String provider;
  @Schema(description = "간편결제 서비스에 등록된 계좌 혹은 현금성 포인트로 결제한 금액")
  private Integer amount;
  @Schema(description = "간편결제 서비스의 적립 포인트나 쿠폰 등으로 즉시 할인된 금액")
  private Integer discountAmount;
}
