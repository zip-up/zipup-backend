package com.zipup.server.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInResponse {
  @Schema(description = "유저 식별자")
  private String id;
  private String name;
  private String email;
  private String profileImage;
}
