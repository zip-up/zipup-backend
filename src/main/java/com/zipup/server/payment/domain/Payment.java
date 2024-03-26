package com.zipup.server.payment.domain;

import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import com.zipup.server.present.domain.Present;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

  @Column
  @NotNull(message = "결제 키 누락")
  private String paymentKey;

  @Column
  @Unique
  @NotNull(message = "주문 번호 누락")
  private String orderId;

  @Column
  @NotNull(message = "결제 가격 누락")
  private Integer price;

  @Column
  private String bank;

  @Column
  @NotNull(message = "결제 수단 누락")
  private String paymentMethod;

  @Transient
  private Present present;

}
