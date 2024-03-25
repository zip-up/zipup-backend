package com.zipup.server.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

  private Integer code;
  private String method;
  private String responseStr;
  private String cardNumber;
  private String accountNumber;
  private String bank;
  private String customerMobilePhone;
  private String message;


}
