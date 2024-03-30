package com.zipup.server.funding.domain;

import com.zipup.server.funding.dto.FundingDetailResponse;
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
import java.util.stream.Collectors;

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

  @Enumerated(EnumType.STRING)
  private GiftCard card;

  @Embedded
  private FundingPeriod fundingPeriod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Setter
  private User user;

  @OneToMany(
          mappedBy = "fund"
          , fetch = FetchType.EAGER)
  private List<Present> presents;


  public FundingSummaryResponse toSummaryResponse() {
    long duration = Duration.between(LocalDateTime.now(), fundingPeriod.getFinishFunding()).toDays();
    int nowPresent = presents.stream()
            .mapToInt(present -> present.getPayment().getPrice())
            .sum();

    int percentage = (int) Math.round(((double) nowPresent / goalPrice) * 100);
    return FundingSummaryResponse.builder()
            .id(id.toString())
            .title(title)
            .imageUrl(!imageUrl.startsWith("https:") ? "https:" + imageUrl : imageUrl)
            .status(duration > 0 ? String.valueOf(duration) : "완료")
            .percent(percentage)
            .organizer(user.getId().toString())
            .build();
  }

  public FundingDetailResponse toDetailResponse(String nowUserId) {
    long duration = Duration.between(LocalDateTime.now(), fundingPeriod.getFinishFunding()).toDays();
    int nowPresent = presents.stream()
            .mapToInt(present -> present.getPayment().getPrice())
            .sum();
    Boolean isOrganizer = nowUserId == null ? false : nowUserId.equals(user.getId().toString());
    boolean isParticipant = presents.stream()
            .anyMatch(p -> p.getUser().getId().toString().equals(nowUserId));
    int percentage = (int) Math.round(((double) nowPresent / goalPrice) * 100);

    return FundingDetailResponse.builder()
            .id(id.toString())
            .title(title)
            .imageUrl(!imageUrl.startsWith("https:") ? "https:" + imageUrl : imageUrl)
            .productUrl(productUrl)
            .description(description)
            .expirationDate(duration > 0 ? duration : 0)
            .isCompleted(duration <= 0)
            .goalPrice(goalPrice)
            .percent(percentage)
            .presentList(presents.stream().map(Present::toSummaryResponse).collect(Collectors.toList()))
            .isOrganizer(isOrganizer)
            .isParticipant(isParticipant)
            .organizer(user.getId().toString())
            .organizerName(user.getName())
            .build();
  }

}
