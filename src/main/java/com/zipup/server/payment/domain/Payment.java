package com.zipup.server.payment.domain;

import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import com.zipup.server.global.util.entity.PaymentStatus;
import com.zipup.server.payment.dto.CancelRecord;
import com.zipup.server.payment.dto.PaymentHistoryResponse;
import com.zipup.server.payment.dto.PaymentResultResponse;
import com.zipup.server.present.domain.Present;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseTimeEntity {

  @Id
  @GeneratedValue(generator = "payment")
  @GenericGenerator(name = "payment", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "payment_id", columnDefinition = "BINARY(16) DEFAULT (UNHEX(REPLACE(UUID(), \"-\", \"\")))")
  @Convert(converter = StringToUuidConverter.class)
  private UUID id;

  @Column(nullable = false, unique = true)
  @Unique
  @NotNull(message = "결제 키 누락")
  private String paymentKey;

  @Column(nullable = false, unique = true)
  @Unique
  @NotNull(message = "주문 번호 누락")
  private String orderId;

  @Column(nullable = false)
  @NotNull(message = "결제 가격 누락")
  private Integer totalAmount;

  @Column
  @Setter
  private Integer balanceAmount;

  @Column
  private String bank;

  @Column
  private String cardNumber;

  @Column
  private String accountNumber;

  @Column
  private String phoneNumber;

  @Column
  private String easyPay;

  @Column
  @NotNull(message = "결제 수단 누락")
  private String paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('READY', 'IN_PROGRESS', 'WAITING_FOR_DEPOSIT', 'DONE', 'CANCELED', 'PARTIAL_CANCELED', 'ABORTED', 'EXPIRED') DEFAULT 'READY'")
  @Setter
  private PaymentStatus paymentStatus;

  @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
  private Present present;

  public PaymentResultResponse toDetailResponse() {
    return PaymentResultResponse.builder()
            .id(id.toString())
            .orderId(orderId)
            .paymentKey(paymentKey)
            .price(balanceAmount)
            .method(paymentMethod)
            .status(paymentStatus + ":" + paymentStatus.getValue())
            .build();
  }
  public PaymentResultResponse toCancelResponse(List<CancelRecord> cancels) {
    return PaymentResultResponse.builder()
            .id(id.toString())
            .orderId(orderId)
            .paymentKey(paymentKey)
            .price(balanceAmount)
            .method(paymentMethod)
            .status(paymentStatus + ":" + paymentStatus.getValue())
            .cancels(cancels)
            .build();
  }

  public PaymentHistoryResponse toHistoryResponse(Boolean refundable) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String historyStatus = getStatusText(paymentStatus);
    String paymentNumber = id.toString().replaceAll("-", "");

    return PaymentHistoryResponse.builder()
            .id(id.toString())
            .fundingName(present.getFund().getTitle())
            .fundingImage(present.getFund().getImageUrl())
            .paymentDate(getCreatedDate().format(formatter))
            .status(historyStatus)
            .amount(balanceAmount)
            .paymentNumber(paymentNumber.substring(0, Math.min(15, paymentNumber.length())))
            .refundable(refundable)
            .build();
  }

  private String getStatusText(PaymentStatus status) {
    switch (status) {
      case DONE:
        return "결제완료";
      case CANCELED:
      case PARTIAL_CANCELED:
        return "취소 완료";
      default:
        return "취소요청";
    }
  }

}
