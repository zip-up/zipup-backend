package com.zipup.server.user.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.AuthenticationUtil;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.TokenAndUserInfoResponse;
import com.zipup.server.user.dto.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_REFRESH_JWT;
import static com.zipup.server.global.exception.CustomErrorCode.TOKEN_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private AuthService authService;
  @Mock
  private JwtProvider jwtProvider;
  @Mock
  private UserService userService;
  @Mock
  private User user;
  @Mock
  private RedisTemplate<String, String> redisTemplate;
  @Mock
  private ValueOperations<String, String> valueOperations;
  @Mock
  private Authentication authentication;

  private final String refreshToken = "mockRefreshToken";
  private final String accessToken = "mockAccessToken";
  private final String userId = UUID.randomUUID().toString();
  private final GrantedAuthority authority = () -> "ROLE_USER";

  @Test
  @DisplayName("access token 으로 로그인 성공")
  void testSignInWithAccessToken_success() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);
      testRefresh_WithValidToken_success();

      when(jwtProvider.generateToken(anyString(), anyString())).thenReturn(new TokenResponse(accessToken, refreshToken));
      when(jwtProvider.getAccessExpirationTime()).thenReturn(10L);
      when(jwtProvider.getRefreshExpirationTime()).thenReturn(10L);
      when(userService.findById(userId)).thenReturn(user);

      TokenAndUserInfoResponse response = authService.signInWithAccessToken();

      assertNotNull(response);
      assertEquals(response.getAccessToken().getValue(), accessToken);
      assertEquals(response.getAccessToken().getMaxAge().toSeconds(), 10L);
      assertEquals(response.getAccessToken().getMaxAge(), response.getRefreshToken().getMaxAge());
      assertEquals(response.getRefreshToken().getValue(), refreshToken);
    }
  }

  @Test
  @DisplayName("access token redis 에 없어 실패")
  void testSignInWithAccessToken_ThrowsException() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(userId + "_REFRESH")).thenReturn(null);

      BaseException thrown = assertThrows(
              BaseException.class,
              () -> authService.signInWithAccessToken()
      );
      assertNotNull(thrown);
      assertEquals(thrown.getStatus(), TOKEN_NOT_FOUND);
    }
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  @DisplayName("refresh token 생성 성공")
  void testRefresh_WithValidToken_success() {
    Collection authorityCollection = List.of(authority);

    when(jwtProvider.getAuthenticationByToken(refreshToken)).thenReturn(authentication);
    when(authentication.getName()).thenReturn(userId);
    when(authentication.getAuthorities()).thenReturn(authorityCollection);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(userId + "_REFRESH")).thenReturn(refreshToken);

    TokenResponse newTokenResponse = new TokenResponse(accessToken, refreshToken);
    when(jwtProvider.generateToken(anyString(), anyString())).thenReturn(newTokenResponse);

    ResponseCookie[] cookies = authService.refresh(refreshToken);

    assertNotNull(cookies);
    assertEquals(2, cookies.length);
    assertEquals(AUTHORIZATION, cookies[0].getName());
    assertEquals(accessToken, cookies[0].getValue());
    assertTrue(cookies[1].getName().contains("refresh"));
    assertEquals(refreshToken, cookies[1].getValue());
  }

  @Test
  @DisplayName("refresh token null 실패")
  void testRefresh_WithNullToken_ThrowsException() {
    BaseException thrown =
            assertThrows(BaseException.class,
                    () -> authService.refresh(null)
            );
    assertNotNull(thrown);
    assertEquals(thrown.getStatus(), EMPTY_REFRESH_JWT);
    verify(jwtProvider, never()).getAuthenticationByToken(accessToken);
  }

  @Test
  @DisplayName("refresh token redis 없어서 실패")
  void testRefresh_NoTokenInRedis_ThrowsException() {
    when(jwtProvider.getAuthenticationByToken(refreshToken)).thenReturn(authentication);
    when(authentication.getName()).thenReturn(userId);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(userId + "_REFRESH")).thenReturn(null);

    BaseException thrown = assertThrows(
            BaseException.class,
            () -> authService.refresh(refreshToken)
    );
    assertNotNull(thrown);
    assertEquals(thrown.getStatus(), TOKEN_NOT_FOUND);
  }

  @Test
  @DisplayName("refresh token redis 달라서 실패")
  void testRefresh_WithInvalidRedisToken_ThrowsException() {
    when(jwtProvider.getAuthenticationByToken(refreshToken)).thenReturn(authentication);
    when(authentication.getName()).thenReturn(userId);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(userId + "_REFRESH")).thenReturn("");

    BaseException thrown = assertThrows(
            BaseException.class,
            () -> authService.refresh(refreshToken)
    );
    assertNotNull(thrown);
    assertEquals(thrown.getStatus(), TOKEN_NOT_FOUND);
  }

}