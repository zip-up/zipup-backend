package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GiftCard implements BaseEnumCode<String> {
  CHUNSIK("춘식");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }
}
