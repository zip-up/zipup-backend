package com.zipup.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPickUpInfoException extends RuntimeException {
  private String column;
  private String row;

  public InvalidPickUpInfoException(String column, String row) {
    super("배송 정보 중 " + column + " 값인 " + row + " 가 올바르지 않아요.");
    this.column = column;
    this.row = row;
  }
}
