package com.zipup.server.global.security.domain;

import org.springframework.data.keyvalue.repository.KeyValueRepository;

import java.util.Optional;

public interface TokenRepository extends KeyValueRepository<Token, String> {
  Optional<Token> findByAccessToken(String accessToken);
  Optional<Token> findByRefreshToken(String refreshToken);
}
