package com.zipup.server.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.zipup.server.*.infrastructure", bootstrapMode = BootstrapMode.DEFERRED)
@RequiredArgsConstructor
public class JpaConfig {
    @PersistenceContext
    private final EntityManager entityManager;
}