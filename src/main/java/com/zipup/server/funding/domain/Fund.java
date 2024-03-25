package com.zipup.server.funding.domain;

import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.*;
import com.zipup.server.user.domain.User;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
  @NotNull(message = "이미지 사진 누락")
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  private GiftCard card;

  @Embedded
  private FundingPeriod fundingPeriod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Setter
  private User user;

}
