package com.zipup.server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
  /**
   * 2000 : Request 오류
   */
  // Common
  REQUEST_ERROR(2000, "입력값을 확인해주세요."),
  EMPTY_ACCESS_JWT( 2001, "Access 토큰을 입력해주세요."),
  EMPTY_REFRESH_JWT( 2002, "Refresh 토큰을 입력해주세요."),
  WRONG_TYPE_TOKEN( 2003, "잘못된 토큰 입니다."),
  UNSUPPORTED_TOKEN( 2004, "지원되지 않는 토큰 입니다."),
  NOT_EXIST_TOKEN(2005,"존재하지 않거나 만료된 토큰입니다. 다시 로그인해주세요."),
  EXPIRED_TOKEN(2006,"만료된 Access 토큰입니다. Refresh 토큰을 이용해서 새로운 Access 토큰을 발급 받으세요."),
  TOKEN_NOT_FOUND( 2007, "토큰이 존재하지 않습니다."),
  ACCESS_DENIED(2008, "권한이 없습니다."),
  DATA_NOT_FOUND(2009, "존재하지 않는 데이터에요."),

  // users
  INVALID_USER_UUID(2101, "유효하지 않은 UUID 입니다. :: "),

  /**
   * 3000 : Response 오류
   */
  // Common
  RESPONSE_ERROR(3000, "값을 불러오는데 실패하였습니다."),

  /**
   * 4000 : 연결 오류
   */
  DATABASE_ERROR(4000, "데이터베이스 연결에 실패하였습니다."),
  SERVER_ERROR(4001, "서버와의 연결에 실패하였습니다."),
  REDIS_ERROR(4002, "redis 연결에 실패하였습니다."),

  /**
   * 5000 : 알 수 없는 오류
   */
  UNKNOWN_ERROR(5002, "알 수 없는 에러가 발생했습니다.");

  private final Integer code;
  private final String message;
}
