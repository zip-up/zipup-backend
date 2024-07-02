package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus implements BaseEnumCode<String> {

  SHIPPING("배송 중."),
  COMPLETED("배송 완료.");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }

}
