package com.zipup.server.user.application;

import com.zipup.server.user.dto.TokenAndUserInfoResponse;
import com.zipup.server.user.dto.TokenResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.CookieUtil;
import com.zipup.server.global.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static com.zipup.server.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository.COOKIE_EXPIRE_SECONDS;
import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final JwtProvider jwtProvider;
  private final RedisTemplate<String, String> redisTemplate;
  private final UserService userService;

  public TokenAndUserInfoResponse signInWithAccessToken(HttpServletRequest request) {
    String accessToken = jwtProvider.resolveToken(request);
    if (accessToken.isBlank()) throw new BaseException(EMPTY_ACCESS_JWT);

    Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);

    String key = authentication.getName();
    String redisRefreshToken = redisTemplate.opsForValue().get(key + "_REFRESH");

    if (redisRefreshToken.isBlank()) throw new BaseException(NOT_EXIST_TOKEN);

    ResponseCookie[] responseCookies = refresh(redisRefreshToken);

    return TokenAndUserInfoResponse.builder()
            .signInResponse(userService.findById(authentication.getName()).toSignInResponse())
            .accessToken(responseCookies[0])
            .refreshToken(responseCookies[1])
            .build();
  }

  @Transactional
  public ResponseCookie[] refresh(String refreshToken) {
    if (refreshToken.isBlank()) throw new BaseException(EMPTY_REFRESH_JWT);
    jwtProvider.verifyRefreshToken(refreshToken);

    Authentication authentication = jwtProvider.getAuthenticationByToken(refreshToken);
    String key = authentication.getName();
    String redisRefreshToken = redisTemplate.opsForValue().get(key + "_REFRESH");

    if (redisRefreshToken.isBlank() || !redisRefreshToken.equals(refreshToken))
      throw new BaseException(NOT_EXIST_TOKEN);

    redisTemplate.delete(key);
    redisTemplate.delete(key + "_REFRESH");

    TokenResponse newToken = jwtProvider.generateToken(
            authentication.getName(),
            authentication.getAuthorities().stream()
                    .findFirst()
                    .orElseThrow(IllegalAccessError::new)
                    .getAuthority()
    );

    return new ResponseCookie[] {
            CookieUtil.addResponseAccessCookie(HttpHeaders.AUTHORIZATION, newToken.getAccessToken(), COOKIE_EXPIRE_SECONDS),
            CookieUtil.addResponseSecureCookie(COOKIE_TOKEN_REFRESH, newToken.getRefreshToken(), COOKIE_EXPIRE_SECONDS)
    };
  }

}
