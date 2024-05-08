package com.zipup.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserException extends RuntimeException {

  private final CustomErrorCode status;
  private final String id;

  public UserException(CustomErrorCode status, String id) {
    super(status.getMessage() + "::" + id);
    this.status = status;
    this.id = status.getMessage() + id;
  }

}
