package com.zipup.server.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResultResponse {
  @Schema(description = "결제 식별자 값")
  private String id;
  @Schema(description = "응답 코드")
  private Integer code;
  @Schema(description = "응답 메시지")
  private String message;
  @Schema(description = "결제 방법")
  private String method;
  @Schema(description = "결제 정보")
  private String responseStr;
  @Schema(description = "카드 번호")
  private String cardNumber;
  @Schema(description = "계좌 번호")
  private String accountNumber;
  @Schema(description = "은행")
  private String bank;
  @Schema(description = "휴대폰 결제 시")
  private String customerMobilePhone;
}
