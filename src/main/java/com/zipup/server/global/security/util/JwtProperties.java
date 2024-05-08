package com.zipup.server.global.security.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Getter
@Setter
public class JwtProperties {
  private String secret;
  private long tokenAccessExpirationTime;
  private long tokenRefreshExpirationTime;
  private String header;
  private String prefix;
  private String suffix;
}
