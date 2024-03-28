package com.zipup.server.global.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class JpaConfigTest {

    @Autowired
    private DataSource dataSource;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private JpaConfig jpaConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jpaConfig = new JpaConfig(entityManager);
    }

    @Test
    public void testJpaDBConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
        } catch (SQLException e) {
            System.err.println(e);
            throw e;
        }
    }

    @Test
    void testJpaConfig() {
        assertNotNull(jpaConfig);
    }
}