package com.zipup.server.user.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.CookieUtil;
import com.zipup.server.global.security.util.JwtProperties;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.user.dto.TokenAndUserInfoResponse;
import com.zipup.server.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_REFRESH_JWT;
import static com.zipup.server.global.exception.CustomErrorCode.TOKEN_NOT_FOUND;
import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final JwtProperties jwtProperties;
  private final JwtProvider jwtProvider;
  private final RedisTemplate<String, String> redisTemplate;
  private final UserService userService;

  public TokenAndUserInfoResponse signInWithAccessToken(String userId) {
    String redisRefreshToken = redisTemplate.opsForValue().get(userId + "_REFRESH");

    if (redisRefreshToken == null) throw new BaseException(TOKEN_NOT_FOUND);

    ResponseCookie[] responseCookies = refresh(redisRefreshToken);

    return TokenAndUserInfoResponse.builder()
            .signInResponse(userService.findById(userId).toSignInResponse())
            .accessToken(responseCookies[0])
            .refreshToken(responseCookies[1])
            .build();
  }

  @Transactional
  public ResponseCookie[] refresh(String refreshToken) {
    if (refreshToken == null) throw new BaseException(EMPTY_REFRESH_JWT);
    jwtProvider.verifyRefreshToken(refreshToken);

    Authentication authentication = jwtProvider.getAuthenticationByToken(refreshToken);
    String key = authentication.getName();
    String redisRefreshToken = redisTemplate.opsForValue().get(key + "_REFRESH");

    if (redisRefreshToken == null || !redisRefreshToken.equals(refreshToken))
      throw new BaseException(TOKEN_NOT_FOUND);

    removeIdInRedisToken(key);

    TokenResponse newToken = jwtProvider.generateToken(
            authentication.getName(),
            authentication.getAuthorities().stream()
                    .findFirst()
                    .orElseThrow(IllegalAccessError::new)
                    .getAuthority()
    );

    return new ResponseCookie[] {
            CookieUtil.addResponseAccessCookie(HttpHeaders.AUTHORIZATION, newToken.getAccessToken(), (int) jwtProperties.getAccessExpirationTime()),
            CookieUtil.addResponseSecureCookie(COOKIE_TOKEN_REFRESH, newToken.getRefreshToken(), (int) jwtProperties.getRefreshExpirationTime())
    };
  }

  public void removeIdInRedisToken(String key) {
    Set<String> keysToDelete = redisTemplate.keys(key + "*");

    if (keysToDelete != null)
      redisTemplate.delete(keysToDelete);
  }

  public boolean signOut(String userId) {
    String accessTokenInRedis = redisTemplate.opsForValue().get(userId);
    String refreshTokenInRedis = redisTemplate.opsForValue().get(userId + "_REFRESH");

    if (accessTokenInRedis == null || refreshTokenInRedis == null) throw new BaseException(TOKEN_NOT_FOUND);

    removeIdInRedisToken(userId);
    SecurityContextHolder.clearContext();
    return true;
  }

}
