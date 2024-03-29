package com.zipup.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class CorsConfig {
  @Value("${server.address}")
  private String server;
  @Value("${client.address}")
  private String client;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin(server);
    configuration.addAllowedOrigin(client);
    configuration.addAllowedMethod("*");
    configuration.addExposedHeader("Authorization");
    configuration.addAllowedHeader("Authorization");
    configuration.addAllowedHeader("Content-Type");

    configuration.setAllowCredentials(true);
    configuration.setAllowedMethods(Collections.singletonList("*"));

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}