package com.zipup.server.funding.dto;

import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FundingAllResponse {
  private UUID id;
  private String title;
  private String description;
  @Schema(description = "펀딩 만료까지 남은 기간, 만료 시 0")
  private int expirationDate;
  @Schema(description = "완료, 진행 여부")
  private Boolean isCompleted;
  @Schema(description = "현재 달성률")
  private int percent;
  private int goalPrice;
  private User user;

  public FundingAllResponse(UUID id, String title, String description, int expirationDate, int percent, int goalPrice, User user) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.expirationDate = expirationDate;
    this.percent = percent;
    this.goalPrice = goalPrice;
    this.user = user;
  }
}
