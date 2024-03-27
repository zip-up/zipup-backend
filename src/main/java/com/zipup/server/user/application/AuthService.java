package com.zipup.server.user.application;

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

import static com.zipup.server.global.exception.CustomErrorCode.NOT_EXIST_TOKEN;
import static com.zipup.server.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository.COOKIE_EXPIRE_SECONDS;
import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final JwtProvider jwtProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public ResponseCookie[] refresh(String refreshToken) {
    jwtProvider.verifyRefreshToken(refreshToken);

    Authentication authentication = jwtProvider.getAuthenticationByToken(refreshToken);
    String redisRefreshToken = redisTemplate.opsForValue().get(authentication.getName() + "_REFRESH");

    if (redisRefreshToken == null || !redisRefreshToken.equals(refreshToken))
      throw new BaseException(NOT_EXIST_TOKEN);

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
