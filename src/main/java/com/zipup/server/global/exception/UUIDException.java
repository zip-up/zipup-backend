package com.zipup.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UUIDException extends RuntimeException {

  private final CustomErrorCode status;

  public UUIDException(CustomErrorCode status) {
    this.status = status;
  }

}

