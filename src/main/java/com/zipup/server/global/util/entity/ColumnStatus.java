package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColumnStatus implements BaseEnumCode<String> {
  PRIVATE("비공개"), PUBLIC("공개");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }
}
