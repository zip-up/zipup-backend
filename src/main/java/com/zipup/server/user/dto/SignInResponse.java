package com.zipup.server.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SignInResponse {
  private UUID id;
  private String name;
  private String email;
}
