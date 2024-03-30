package com.zipup.server.present.domain;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.user.domain.User;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "presents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Present extends BaseTimeEntity {

  @Id
  @GeneratedValue(generator = "present")
  @GenericGenerator(name = "present", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "present_id", columnDefinition = "BINARY(16) DEFAULT (UNHEX(REPLACE(UUID(), \"-\", \"\")))")
  @Convert(converter = StringToUuidConverter.class)
  private UUID id;

  @Column
  @NotNull(message = "참여자 이름 누락")
  private String senderName;

  @Column
  private String congratsMessage;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  @Setter
  private User user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fund_id")
  @Setter
  private Fund fund;

  @OneToOne(fetch = FetchType.EAGER)
  private Payment payment;

  public PresentSummaryResponse toSummaryResponse() {
    int percentage = (int) Math.round(((double) payment.getPrice() / fund.getGoalPrice()) * 100);
    System.out.println("myParticipate :: " + payment.getPrice() + " goalPrice :: " + fund.getGoalPrice());

    return PresentSummaryResponse.builder()
            .id(id.toString())
            .senderName(senderName)
            .profileImage(user.getProfileImage() != null ? user.getProfileImage() : "")
            .congratsMessage(congratsMessage)
            .participantId(user.getId().toString())
            .paymentId(payment.getId().toString())
            .contributionPercent(percentage)
            .build();
  }

}
