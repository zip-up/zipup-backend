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
public class PaymentHistoryResponse {
  @Schema(description = "결제 식별자 값")
  private String id;
  @Schema(description = "펀딩 이름")
  private String fundingName;
  @Schema(description = "펀딩 이미지")
  private String fundingImage;
  @Schema(description = "결제 일시", example = "yyyy-MM-dd HH:mm:ss")
  private String paymentDate;
  @Schema(description = "특정 펀딩의 최근 결제일", example = "yyyy-MM-dd HH:mm:ss")
  private String mostRecentPaymentDateInFunding;
  @Schema(description = "가격")
  private Integer amount;
  @Schema(description = "결제 번호")
  private String paymentNumber;
  @Schema(description = "결제 상태", example = "결제완료 || 취소완료 || 취소요청")
  private String status;
  @Schema(description = "결제 취소 가능 여부")
  private Boolean refundable;
  @Schema(description = "결제 수단 가상계좌 여부")
  private Boolean isVirtualAccount;
  @Schema(description = "입금 처리 완료 여부")
  private Boolean isDepositCompleted;
  @Schema(description = "가상 계좌 정보")
  private VirtualAccount virtualAccount;
}
