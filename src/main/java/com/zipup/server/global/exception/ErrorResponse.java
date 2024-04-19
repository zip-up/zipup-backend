package com.zipup.server.global.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ErrorResponse {
  private final int code;
  private final String message;
  private final String error_name;

  public ErrorResponse(int code, String message, String name) {
    this.code = code;
    this.message = message;
    this.error_name = name;
  }

  public static ErrorResponse toErrorResponse(CustomErrorCode errorCode) {
    return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            errorCode.name());
  }
}
