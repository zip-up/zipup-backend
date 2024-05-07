package com.zipup.server.user.presentation;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.user.application.AuthService;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import java.util.Date;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_REFRESH_JWT;
import static com.zipup.server.global.exception.CustomErrorCode.UNSUPPORTED_TOKEN;
import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

  @MockBean
  private AuthService mockAuthService;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private EntityManager entityManager;
  @Autowired
  private JwtProvider jwtProvider;

  private final String REFRESH_END_POINT = "/api/v1/auth/refresh";
  private final String SIGNOUT_END_POINT = "/api/v1/auth/sign-out";
  private String userId;
  private User user;
  private TokenResponse tokenResponse;

  @BeforeEach
  void setUp() {
    user = User.builder()
            .email("mock@mock.com")
            .build();
    entityManager.persist(user);
    entityManager.flush();
    userId = user.getId().toString();

    tokenResponse = jwtProvider.generateToken(userId, String.valueOf(UserRole.USER));
  }

  @Test
  public void refreshTest_failure_nullToken() throws Exception {
    mockMvc.perform(post(REFRESH_END_POINT)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BaseException))
            .andExpect(result -> {
              BaseException exception = (BaseException) result.getResolvedException();
              assertNotNull(exception);
              assertEquals(EMPTY_REFRESH_JWT.getCode(), exception.getStatus().getCode());
            });
  }

  @Test
  public void refreshTest_failure_blankToken() throws Exception {
    mockMvc.perform(post(REFRESH_END_POINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(new Cookie(COOKIE_TOKEN_REFRESH, "")))
            .andExpect(status().isUnauthorized())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BaseException))
            .andExpect(result -> {
              BaseException exception = (BaseException) result.getResolvedException();
              assertNotNull(exception);
              assertEquals(EMPTY_REFRESH_JWT.getCode(), exception.getStatus().getCode());
            });
  }

  @Test
  public void refreshTest_failure_invalidToken() throws Exception {
    Date now = new Date();
    Date expireDate = new Date(now.getTime());
    byte[] bytes = Decoders.BASE64.decode(new String("invalidSecretKey20240505invalidSecretKey20240505invalidSecretKey20240505invalidSecretKey20240505invalidSecretKey20240505invalidSecretKey20240505"));

    String invalidToken = Jwts.builder()
            .setClaims(Jwts.claims().setId("id"))
            .setIssuedAt(now)
            .setExpiration(expireDate)
            .signWith(Keys.hmacShaKeyFor(bytes), SignatureAlgorithm.HS512)
            .compact();

    when(mockAuthService.refresh(any())).thenThrow(new BaseException(UNSUPPORTED_TOKEN));

    mockMvc.perform(post(REFRESH_END_POINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(new Cookie(COOKIE_TOKEN_REFRESH, invalidToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BaseException))
            .andExpect(result -> {
              BaseException exception = (BaseException) result.getResolvedException();
              assertNotNull(exception);
              assertEquals(UNSUPPORTED_TOKEN.getCode(), exception.getStatus().getCode());
            });
  }

  @Test
  @WithMockUser
  public void signOutTest_fail() throws Exception {
    mockMvc.perform(post(SIGNOUT_END_POINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, tokenResponse.getAccessToken())
                    .cookie(new Cookie(COOKIE_TOKEN_REFRESH, tokenResponse.getRefreshToken())))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("{\"id\": \"fail\"}"));
  }

}