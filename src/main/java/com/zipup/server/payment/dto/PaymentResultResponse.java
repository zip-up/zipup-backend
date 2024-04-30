package com.zipup.server.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResultResponse {
  @Schema(description = "결제 식별자 값")
  private String id;
  @Schema(description = "결제 방법")
  private String method;
  private String orderId;
  private String paymentKey;
  private Integer price;
  @Schema(description = "결제 처리 상태")
  private String status;
  @Schema(description = "취소 내역")
  private List<CancelRecord> cancels;
}
