package com.zipup.server.global.security;

import com.zipup.server.global.security.util.JwtProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class JwtProviderTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @InjectMocks
  private JwtProvider jwtProvider;

}
