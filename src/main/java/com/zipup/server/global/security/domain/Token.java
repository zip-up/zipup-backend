package com.zipup.server.global.security.domain;

import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("token")
public class Token {
  @Id
  private String id;

  @Indexed
  private String accessToken;

  private String refreshToken;

  @TimeToLive(unit = TimeUnit.MILLISECONDS)
  private Long timeToLive;
}
