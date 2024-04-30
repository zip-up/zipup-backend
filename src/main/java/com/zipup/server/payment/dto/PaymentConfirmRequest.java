package com.zipup.server.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentConfirmRequest {
  private String orderId;
  private Integer amount;
  private String paymentKey;
}
