package com.zipup.server.global.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus implements BaseEnumCode<String> {
  READY("결제를 생성하면 가지게 되는 초기 상태"),
  IN_PROGRESS("결제수단 정보와 해당 결제수단의 소유자가 맞는지 인증을 마친 상태. 30분 안에 결제 승인 API를 호출하면 결제 완료."),
  EXPIRED("결제 유효 시간 30분이 지나 거래가 취소된 상태."),
  WAITING_FOR_DEPOSIT("결제 고객이 발급된 가상계좌에 입금하는 것을 기다리고 있는 상태."),
  DONE("요청한 결제가 승인된 상태."),
  CANCELED("승인된 결제가 취소된 상태."),
  PARTIAL_CANCELED("승인된 결제가 부분 취소된 상태."),
  INVALID_PAYMENT_STATUS("사용 불가한 결제 상태."),
  ABORTED("결제 승인이 실패한 상태.");

  private final String value;

  @Override
  public String getValue() {
    return this.value;
  }
}
