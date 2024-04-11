package com.zipup.server.user.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
public class TokenAndUserInfoResponse {
  private ResponseCookie accessToken;
  private ResponseCookie refreshToken;
  private ResponseCookie tempToken;
  private SignInResponse signInResponse;
}
