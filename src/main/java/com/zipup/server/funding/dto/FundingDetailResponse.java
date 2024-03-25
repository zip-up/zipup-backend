package com.zipup.server.funding.dto;

import com.zipup.server.present.dto.PresentSummaryResponse;
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
  private String status;
  private Integer percent;
  private Integer goalPrice;
  private List<PresentSummaryResponse> presentList;
}
