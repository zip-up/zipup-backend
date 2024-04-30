package com.zipup.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class UniqueConstraintException extends RuntimeException {
  private String key;
  private String value;

  public UniqueConstraintException(String key, String value) {
    super(key + " 키에 대한 " + value + " 값이 이미 존재해요.");
    this.key = key;
    this.value = value;
  }
}
