package com.zipup.server.funding.dto;

import com.zipup.server.present.dto.PresentSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class FundingDetailResponse {
  private String id;
  private String title;
  private String description;
  private String imageUrl;
  @Schema(description = "펀딩 만료까지 남은 기간, 만료 시 0")
  private Long expirationDate;
  @Schema(description = "완료, 진행 여부")
  private Boolean isCompleted;
  @Schema(description = "현재 달성률")
  private Integer percent;
  private Integer goalPrice;
  @Schema(description = "해당 펀딩에 참여한 사람 목록")
  private List<PresentSummaryResponse> presentList;
  private Boolean isOrganizer;
  private String organizer;
}
