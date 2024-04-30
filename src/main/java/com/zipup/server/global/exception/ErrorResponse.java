package com.zipup.server.global.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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

  @JsonCreator
  public ErrorResponse(@JsonProperty("code") String code,
                       @JsonProperty("message") String message) {
    this.code = 0;
    this.message = message;
    this.error_name = code;
  }

  public static ErrorResponse toErrorResponse(CustomErrorCode errorCode) {
    return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            errorCode.name());
  }
}
