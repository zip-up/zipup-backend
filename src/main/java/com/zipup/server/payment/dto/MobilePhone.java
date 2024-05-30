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
public class MobilePhone {
  @Schema(description = "휴대폰 정보")
  private String customerMobilePhone;
//  private CustomerMobilePhone customerMobilePhone;
  @Schema(description = "휴대폰 결제 내역 영수증")
  private String receiptUrl;
  @Schema(description = "정산 상태. 'INCOMPLETED', 'COMPLETED'")
  private String settlementStatus;
}
