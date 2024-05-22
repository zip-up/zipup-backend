package com.zipup.server.funding.dto;

import com.zipup.server.present.dto.PresentSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FundingDetailResponse {
  private UUID id;
  private String title;
  private String description;
  private String imageUrl;
  private String productUrl;
  @Schema(description = "펀딩 만료까지 남은 기간, 만료 시 0")
  private int expirationDate;
  @Schema(description = "완료, 진행 여부")
  private Boolean isCompleted;
  @Schema(description = "현재 달성률")
  private int percent;
  private int goalPrice;
  @Schema(description = "해당 펀딩에 참여한 사람 목록")
  private List<PresentSummaryResponse> presentList;
  private Boolean isOrganizer;
  private Boolean isParticipant;
  private UUID organizer;
  private String organizerName;

  public FundingDetailResponse(UUID id, String title, String description, String imageUrl, String productUrl, int expirationDate, int percent, int goalPrice, UUID organizer, String organizerName) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.productUrl = productUrl;
    this.expirationDate = expirationDate;
    this.percent = percent;
    this.goalPrice = goalPrice;
    this.organizer = organizer;
    this.organizerName = organizerName;
  }
}
