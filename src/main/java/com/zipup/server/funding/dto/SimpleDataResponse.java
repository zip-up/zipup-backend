package com.zipup.server.funding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SimpleDataResponse {
  @Schema(description = "펀딩 식별자 값 (UUID)")
  private final String id;
}
