package com.zipup.server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

  @Test
  void testOpenAPI() {
    // Given
    SwaggerConfig swaggerConfig = new SwaggerConfig();

    // When
    OpenAPI openAPI = swaggerConfig.openAPI();

    // Then
    assertNotNull(openAPI);
    assertNotNull(openAPI.getInfo());
    assertNotNull(openAPI.getComponents());
  }

  @Test
  void testApiInfo() {
    // Given
    SwaggerConfig swaggerConfig = new SwaggerConfig();

    // When
    Info apiInfo = swaggerConfig.apiInfo();

    // Then
    assertNotNull(apiInfo);
    assertEquals("v1.0.0", apiInfo.getVersion());
    assertEquals("\uD83C\uDFE0\uD83C\uDF81️ ZIPUP API 명세서", apiInfo.getTitle());
    assertEquals("ZIPUP API 명세서", apiInfo.getDescription());
  }
}

