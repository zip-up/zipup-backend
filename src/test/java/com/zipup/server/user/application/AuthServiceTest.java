package com.zipup.server.user.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.TokenAndUserInfoResponse;
import com.zipup.server.user.dto.TokenResponse;
import com.zipup.server.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static com.zipup.server.global.exception.CustomErrorCode.TOKEN_NOT_FOUND;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest
@Transactional
public class AuthServiceTest {
  @Autowired
  private AuthService authService;
  @Autowired
  private JwtProvider jwtProvider;
  @Autowired
  private UserRepository userRepository;

  private User user;
  private String userId;
  private TokenResponse tokenResponse;
  private MockHttpServletRequest request = new MockHttpServletRequest();

  @BeforeEach
  void setUp() {
    user = User.builder()
            .email("mock@mock.com")
            .build();
    userRepository.save(user);

    userId = userRepository.findUserByEmail("mock@mock.com").getId().toString();

    tokenResponse = jwtProvider.generateToken(userId, String.valueOf(UserRole.USER));
  }

  @Test
  public void signInWithAccessToken_throwNoAuthentication() {
    request.addHeader(AUTHORIZATION, tokenResponse.getAccessToken());
    Exception thrown = assertThrows(Exception.class, () -> authService.signInWithAccessToken(request));
    assertNotNull(thrown);
    assertEquals(NullPointerException.class.getName(), thrown.toString());
  }

  @Test
  @WithMockUser
  public void signInWithAccessToken_throwNoTokenInRedis() {
    request.addHeader(AUTHORIZATION, tokenResponse.getAccessToken());
    BaseException thrown = assertThrows(BaseException.class, () -> authService.signInWithAccessToken(request));
    assertNotNull(thrown);
    assertEquals(TOKEN_NOT_FOUND, thrown.getStatus());
  }

  @Test
  public void signInWithAccessToken_success() {
    Authentication authentication = jwtProvider.getAuthenticationByToken(tokenResponse.getAccessToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    request.addHeader(AUTHORIZATION, tokenResponse.getAccessToken());

    TokenAndUserInfoResponse response = authService.signInWithAccessToken(request);

    assertNotNull(response);
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());

    assertNotEquals(response.getAccessToken().getValue(), response.getRefreshToken().getValue());
  }

  @Test
  public void signOut_success() {
    Authentication authentication = jwtProvider.getAuthenticationByToken(tokenResponse.getAccessToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    request.addHeader(AUTHORIZATION, tokenResponse.getAccessToken());

    assertTrue(authService.signOut(request));
  }

  @Test
  @WithMockUser
  public void signOut_throwNoTokenInRedis() {
    request.addHeader(AUTHORIZATION, tokenResponse.getAccessToken());

    BaseException thrown = assertThrows(BaseException.class, () -> authService.signOut(request));
    assertNotNull(thrown);
    assertEquals(TOKEN_NOT_FOUND, thrown.getStatus());
  }

}
