package com.zipup.server.funding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class SimpleFundingDataResponse {
  @Schema(description = "펀딩 식별자 값 (UUID)")
  private final String id;
  @Schema(description = "펀딩 이미지")
  private final String imageUrl;
}
