package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginProvider implements BaseEnumCode<String> {
  GOOGLE( "구글"), NAVER("네이버"), KAKAO( "카카오톡"), LOCAL("로컬");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }
}
