package com.zipup.server.global.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
public class ErrorResponse {
  private final int code;
  private final String message;
  private final List<String> errors;

  public ErrorResponse(int code, String message, String error) {
    this.code = code;
    this.message = message;
    this.errors = List.of(error);
  }

  public static ErrorResponse toErrorResponse(CustomErrorCode errorCode) {
    return new ErrorResponse(
            errorCode.getCode(),
            errorCode.name(),
            errorCode.getMessage());
  }
}
