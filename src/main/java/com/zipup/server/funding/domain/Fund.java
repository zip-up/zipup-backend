package com.zipup.server.funding.domain;

import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.*;
import com.zipup.server.present.domain.Present;
import com.zipup.server.user.domain.User;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fundings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Fund extends BaseTimeEntity {

  @Id
  @GeneratedValue(generator = "fund")
  @GenericGenerator(name = "fund", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "fund_id", columnDefinition = "BINARY(16) DEFAULT (UNHEX(REPLACE(UUID(), \"-\", \"\")))")
  @Convert(converter = StringToUuidConverter.class)
  private UUID id;

  @Column
  @NotNull(message = "펀딩 제목 누락")
  private String title;

  @Column
  @NotNull(message = "주소 누락")
  private String roadAddress;

  @Column
  @NotNull(message = "상세 주소 누락")
  private String detailAddress;

  @Column
  @NotNull(message = "주최자 전화번호 누락")
  private String phoneNumber;

  @Column(columnDefinition = "text")
  private String description;

  @Column
  @NotNull(message = "목표 가격 누락")
  private Integer goalPrice;

  @Column
  @NotNull(message = "상품 주소 누락")
  private String productUrl;

  @Column
  @Setter
  private String imageUrl;

  @Column
  @Setter
  private String cancelReason;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('CHUNSIK')")
  private GiftCard card;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('SHIPPING', 'COMPLETED') DEFAULT 'SHIPPING'")
  private DeliveryStatus deliveryStatus;

  @Embedded
  private FundingPeriod fundingPeriod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Setter
  private User user;

  @OneToMany(
          mappedBy = "fund"
          , fetch = FetchType.LAZY)
  private List<Present> presents;

  private String formatImageUrl() {
    if (imageUrl == null || imageUrl.isEmpty()) return imageUrl;
    return imageUrl.startsWith("//") ? "https:" + imageUrl : imageUrl;
  }

  private int calculatePercentage() {
    int nowPresent = presents.stream()
            .filter(present -> present.getStatus().equals(ColumnStatus.PUBLIC))
            .mapToInt(present -> present.getPayment().getBalanceAmount())
            .sum();
    return (int) Math.round(((double) nowPresent / goalPrice) * 100);
  }

  public FundingSummaryResponse toSummaryResponse() {
    return FundingSummaryResponse.builder()
            .id(id)
            .title(title)
            .imageUrl(formatImageUrl())
            .percent(calculatePercentage())
            .dDay((int) Duration.between(LocalDateTime.now(), fundingPeriod.getFinishFunding()).toDays())
            .organizer(user.getId())
            .build();
  }

}
