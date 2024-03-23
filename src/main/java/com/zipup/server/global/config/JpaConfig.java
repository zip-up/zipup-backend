package com.zipup.server.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaConfig {
    @PersistenceContext
    private final EntityManager entityManager;
}