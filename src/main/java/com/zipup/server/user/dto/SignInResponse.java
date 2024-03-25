package com.zipup.server.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SignInResponse {
  @Schema(description = "유저 식별자")
  private UUID id;
  private String name;
  private String email;
}
