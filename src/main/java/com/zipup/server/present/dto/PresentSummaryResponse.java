package com.zipup.server.present.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PresentSummaryResponse {
  private String id;
  private String senderName;
  private Integer contributionPercent;
  private String profileImage;
  private String congratsMessage;
}
