package com.zipup.server.funding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class FundingSummaryResponse {
  private UUID id;
  private String title;
  private String imageUrl;
  private int dDay;
  private Integer percent;
  private UUID organizer;
}
