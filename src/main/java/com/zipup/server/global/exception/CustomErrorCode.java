package com.zipup.server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
  /**
   * 2000 : token 오류
   */
  EMPTY_ACCESS_JWT( 2001, "Access 토큰을 입력해주세요."),
  EMPTY_REFRESH_JWT( 2002, "Refresh 토큰을 입력해주세요."),
  WRONG_TYPE_TOKEN( 2003, "잘못된 토큰 입니다."),
  UNSUPPORTED_TOKEN( 2004, "지원되지 않는 토큰 입니다."),
  TOKEN_NOT_FOUND( 2005, "토큰이 존재하지 않습니다."),
  EXPIRED_TOKEN(2006,"만료된 Access 토큰입니다. Refresh 토큰을 이용해서 새로운 Access 토큰을 발급 받으세요."),
  ACCESS_DENIED(2007, "권한이 없습니다."),
  EXPIRED_REFRESH_TOKEN(2008,"만료된 Refresh 토큰입니다. 다시 로그인해 주세요."),

  /**
   * 3000 : 비즈니스 오류
   */
  INVALID_USER_UUID(3001, "유효하지 않은 UUID 입니다. :: "),
  DATA_NOT_FOUND(3002, "존재하지 않는 데이터에요."),
  UNIQUE_CONSTRAINT(3003, "Unique 키가 충돌해요"),
  ACTIVE_FUNDING(3004, "진행 중인 펀딩 존재"),
  NOT_CANCELABLE_AMOUNT(3005, "취소 할 수 없는 금액 입니다."),
  ALREADY_CANCEL(3006, "이미 취소된 결제입니다."),
  WITHDRAWAL_USER(3007, "탈퇴한 회원입니다."),
  INVALID_USER_INFO(3008, "잘못된 입력 정보입니다."),

  /**
   * 4000 : 연결 오류
   */
  DATABASE_ERROR(4000, "데이터베이스 연결에 실패하였습니다."),
  SERVER_ERROR(4001, "서버와의 연결에 실패하였습니다."),
  REDIS_ERROR(4002, "redis 연결에 실패하였습니다."),
  TOSS_ERROR(4003, "toss payment 연결에 실패하였습니다."),

  /**
   * 5000 : 알 수 없는 오류
   */
  UNKNOWN_ERROR(5002, "알 수 없는 에러가 발생했습니다.");

  private final Integer code;
  private final String message;
}
