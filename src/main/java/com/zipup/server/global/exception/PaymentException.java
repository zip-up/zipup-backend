package com.zipup.server.global.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

  int status;
  String message;
  String code;

  public PaymentException(int status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

}
