package com.zipup.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardInformation {
  @Schema(description = "카드사에 결제 요청한 금액")
  private Integer cancelAmount;
  @Schema(description = "카드 발급사 숫자 코드")
  private String issuerCode;
  @Schema(description = "카드 매입사 숫자 코드")
  private String acquirerCode;
  @Schema(description = "일부 마스킹 된 카드 번호")
  private String number;
  @Schema(description = "할부 개월 수. 일시불이면 0")
  private Integer installmentPlanMonths;
  @Schema(description = "카드사 승인 번호")
  private String approveNo;
  @Schema(description = "카드사 포인트 사용 여부")
  private Boolean useCardPoint;
  @Schema(description = "무이자 할부 적용 여부")
  private Boolean isInterestFree;
  @Schema(description = "카드 종류. '신용', '체크', '기프트', '미확인' 중 하나")
  private String cardType;
  @Schema(description = "카드의 소유자 타입. '개인', '법인', '미확인' 중 하나")
  private String ownerType;
  @Schema(description = "카드 결제의 매입 상태.\n" +
          "- READY: 아직 매입 요청이 안 된 상태.\n" +
          "- REQUESTED: 매입이 요청된 상태.\n" +
          "- COMPLETED: 요청된 매입이 완료된 상태.\n" +
          "- CANCEL_REQUESTED: 매입 취소가 요청된 상태.\n" +
          "- CANCELED: 요청된 매입 취소가 완료된 상태.' 중 하나")
  private String acquireStatus;
  @Schema(description = "할부가 적용된 결제에서 할부 수수료를 부담하는 주체.\n" +
          "- BUYER: 상품을 구매한 고객이 할부 수수료를 부담. 일반적인 할부 결제.\n" +
          "- CARD_COMPANY: 카드사에서 할부 수수료를 부담. 무이자 할부 결제.\n" +
          "- MERCHANT: 상점에서 할부 수수료를 부담. 무이자 할부 결제.'\n 중 하나")
  private String interestPayer;

}
