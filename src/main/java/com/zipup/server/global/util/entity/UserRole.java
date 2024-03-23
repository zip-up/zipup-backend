package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole implements BaseEnumCode<String> {
  ADMIN("관리자"), USER("회원"), GUEST("비회원");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }
}
