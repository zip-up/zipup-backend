package com.zipup.server.user.dto;

import com.zipup.server.global.util.entity.LoginProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInRequest {
  private String email;
  private String password;

  public User toEntity() {
    return User.builder()
            .email(email)
            .role(UserRole.USER)
            .loginProvider(LoginProvider.KAKAO)
            .build();
  }
}
