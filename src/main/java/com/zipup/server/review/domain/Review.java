package com.zipup.server.review.domain;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import com.zipup.server.user.domain.User;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

  @Id
  @GeneratedValue(generator = "review")
  @GenericGenerator(name = "review", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "review_id", columnDefinition = "BINARY(16) DEFAULT (UNHEX(REPLACE(UUID(), \"-\", \"\")))")
  @Convert(converter = StringToUuidConverter.class)
  private UUID id;

  @Column
  @NotNull(message = "참여자 이름 누락")
  private String senderName;

  @Column(columnDefinition = "text")
  private String description;

  @Column
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Setter
  private User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fund_id")
  private Fund fund;

}
