package com.zipup.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentHistoryResponse {
  @Schema(description = "결제 식별자 값")
  private String id;
  @Schema(description = "펀딩 이름")
  private String fundingName;
  @Schema(description = "펀딩 이미지")
  private String fundingImage;
  @Schema(description = "결제 일시", example = "yyyy-MM-dd HH:mm:ss")
  private String paymentDate;
  @Schema(description = "가격")
  private Integer amount;
  @Schema(description = "결제 번호")
  private String paymentNumber;
  @Schema(description = "결제 상태")
  private String status;
  @Schema(description = "결제 취소 가능 여부")
  private Boolean refundable;
}