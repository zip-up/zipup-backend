package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus implements BaseEnumCode<String> {
  NONE("환불 요청이 없는 상태."),
  PENDING("환불을 처리 중인 상태."),
  FAILED("환불에 실패한 상태."),
  PARTIAL_FAILED("부분 환불에 실패한 상태."),
  COMPLETED("환불이 완료된 상태.");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }
}
