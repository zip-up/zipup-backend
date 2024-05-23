package com.zipup.server.funding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FundingCancelRequest {
  @Schema(description = "펀딩 id")
  private String fundingId;
  @Schema(description = "취소 사유", required = true)
  private String cancelReason;
}
