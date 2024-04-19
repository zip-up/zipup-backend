package com.zipup.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  private final CustomErrorCode status;

  public ResourceNotFoundException(CustomErrorCode status) {
    this.status = status;
  }

}
