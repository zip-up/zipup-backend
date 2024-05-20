package com.zipup.server.funding.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FundingSummaryResponse {
  private String id;
  private String title;
  private String imageUrl;
  private String status;
  private long dDay;
  private Integer percent;
  private String organizer;
}
