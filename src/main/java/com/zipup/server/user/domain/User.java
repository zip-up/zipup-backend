package com.zipup.server.user.domain;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import com.zipup.server.global.util.entity.LoginProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.present.domain.Present;
import com.zipup.server.review.domain.Review;
import com.zipup.server.user.dto.SignInResponse;
import com.zipup.server.user.dto.UserListResponse;
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

  @Column
  private String name;

  @Column
  private String profileImage;

  @Column
  private String password;

  @Column(nullable = false, unique = true)
  @Email(message = "메일 형식에 맞춰 작성해주세요",
          regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
  @NotNull(message = "메일 주소 누락")
  private String email;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  private LoginProvider loginProvider;

  @OneToMany(
          mappedBy = "user"
          , fetch = FetchType.LAZY
          , cascade = CascadeType.PERSIST
          , orphanRemoval = true
  )
  private List<Fund> funds;

  @OneToMany(
          mappedBy = "user"
          , fetch = FetchType.LAZY
          , cascade = CascadeType.PERSIST
          , orphanRemoval = true
  )
  private List<Present> presents;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Review> reviews;

  public UserListResponse toResponseList() {
    return UserListResponse.builder()
            .id(id.toString())
            .name(name)
            .email(email)
//            .fundList(funds.stream().map(Fund::toSimpleDataResponse).collect(Collectors.toList()))
//            .presentList(presents.stream().map(Present::toSimpleDataResponse).collect(Collectors.toList()))
            .build();
  }

  public SimpleDataResponse toSimpleDataResponse() {

    return SimpleDataResponse.builder()
            .id(id.toString())
            .build();
  }

  public SignInResponse toSignInResponse() {

    return SignInResponse.builder()
            .id(id.toString())
            .name(name)
            .email(email)
            .profileImage(profileImage != null ? profileImage : "")
            .build();
  }

}
