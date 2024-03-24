package com.zipup.server.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.cloud.config.enabled=false")
@Transactional
class RedisConfigTest {

  @Autowired
  private LettuceConnectionFactory lettuceConnectionFactory;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Test
  @DisplayName("redis 연결 실패 테스트")
  public void testRedisDisConnection() {
    redisTemplate.setConnectionFactory(new LettuceConnectionFactory("localhost", 1111));

    assertThrows(IllegalStateException.class, ()
            -> redisTemplate.opsForValue().get("key"));
  }

  @Test
  @DisplayName("redis 연결 테스트")
  public void testRedisConnection() {
    RedisConnection connection = lettuceConnectionFactory.getConnection();
    assertNotNull(connection);
    connection.close();
  }

  @Test
  @DisplayName("redis 삽입 후 조회")
  public void testRedisTemplateUsage() {
    redisTemplate.opsForValue().set("test-key", "test-value");

    String result = redisTemplate.opsForValue().get("test-key");

    assertEquals("test-value", result);
    redisTemplate.delete("test-key");
  }

}
