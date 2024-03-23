package com.zipup.server.user.domain;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import com.zipup.server.global.util.entity.LoginProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.present.domain.Present;
import com.zipup.server.review.domain.Review;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(generator = "user")
  @GenericGenerator(name = "user", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "user_id", columnDefinition = "BINARY(16) DEFAULT (UNHEX(REPLACE(UUID(), \"-\", \"\")))")
  @Convert(converter = StringToUuidConverter.class)
  private UUID id;

  private String name;

  @Column(nullable = false, unique = true)
  @Email(message = "메일 형식에 맞춰 작성해주세요",
          regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
  @NotNull(message = "메일 주소 누락")
  private String email;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  private LoginProvider socialProvider;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Fund> funds;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Present> presents;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Review> reviews;

}
