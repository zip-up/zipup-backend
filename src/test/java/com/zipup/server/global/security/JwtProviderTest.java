package com.zipup.server.global.security;

import com.zipup.server.global.security.util.JwtProperties;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.security.Key;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtProviderTest {

  class TestableJwtProvider extends JwtProvider {
    public TestableJwtProvider(RedisTemplate<String, String> redisTemplate, JwtProperties jwtProperties) {
      super(redisTemplate, jwtProperties);
    }

    @Override
    public Key createSecretKey(String secret) {
      return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }
  }

  @InjectMocks
  private JwtProvider jwtProvider;
  @Mock
  private RedisTemplate<String, String> redisTemplate;
  @Mock
  private ValueOperations<String, String> valueOperations;
  @Mock
  private User user;
  @Mock
  private JwtProperties mockJwtProperties;
  private JwtProperties jwtProperties;
  private String userId = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
//    when(mockJwtProperties.getSecret()).thenReturn(jwtProperties.getSecret());
//    when(mockJwtProperties.getTokenAccessExpirationTime()).thenReturn(jwtProperties.getTokenAccessExpirationTime());
//    when(mockJwtProperties.getTokenRefreshExpirationTime()).thenReturn(jwtProperties.getTokenRefreshExpirationTime());
//    when(mockJwtProperties.getHeader()).thenReturn(jwtProperties.getHeader());
//    when(mockJwtProperties.getPrefix()).thenReturn(jwtProperties.getPrefix());
//    when(mockJwtProperties.getSuffix()).thenReturn(jwtProperties.getSuffix());
//
//    jwtProvider.init();
    TestableJwtProvider jwtProvider = new TestableJwtProvider(redisTemplate, mockJwtProperties);
    jwtProvider.createSecretKey("TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505");
  }

  @Test
  void testInit() {
    when(mockJwtProperties.getSecret()).thenReturn("TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505");

    jwtProvider.init();

    assertNotNull(jwtProvider.getSecretKey());
    assertNotNull(jwtProvider.getJwtProperties());
  }

  @Test
  void testGenerateAccessToken() {
//    when(mockJwtProperties.getSecret()).thenReturn("TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505TestSecretKey20240505");
//    jwtProvider.init();

    Claims claims = Jwts.claims().setId(userId);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

//    when(jwtProvider.getSecretKey()).thenReturn(valueOperations);

    when(jwtProvider.generateAccessToken(claims)).thenReturn("accessToken");
    when(jwtProvider.generateRefreshToken(claims)).thenReturn("refreshToken");

//    String response = jwtProvider.generateAccessToken(claims);
    TokenResponse response = jwtProvider.generateToken(userId, UserRole.USER.getKey());

    assertNotNull(response);
  }

}
