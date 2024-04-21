package com.zipup.server.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zipup.server.global.util.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentResponse {
  @Schema(description = "Toss Payment 객체의 날짜 기반 버저닝")
  private String version;
  @Schema(description = "결제 키 값")
  private String paymentKey;
  @Schema(description = "주문 번호")
  private String orderId;
  @Schema(description = "구매 상품")
  private String orderName;
  @Schema(description = "상점 아이디")
  private String mId;
  @Schema(description = "결제 타입 정보")
  private String type;
  @Schema(description = "결제 수단")
  private String method;
  @Schema(description = "총 결제 금액")
  private Integer totalAmount;
  @Schema(description = "취소 가능 금액")
  private Integer balanceAmount;
  @Schema(description = "결제 처리 상태")
  private PaymentStatus status;
  @Schema(description = "결제가 일어난 날짜와 시간 정보")
  private OffsetDateTime requestedAt;
  @Schema(description = "결제 승인이 일어난 날짜와 시간 정보")
  private OffsetDateTime approvedAt;
  @Schema(description = "마지막 거래의 키 값. 한 결제 건의 승인 거래와 취소 거래를 구분하는 데 사용")
  private String lastTransactionKey;
  @Schema(description = "결제 취소 이력")
  private List<CancelRecord> cancels;
  @Schema(description = "카드로 결제하면 제공되는 카드 관련 정보")
  private CardInformation card;
  @Schema(description = "가상계좌로 결제하면 제공되는 가상계좌 관련 정보")
  private VirtualAccount virtualAccount;
}
