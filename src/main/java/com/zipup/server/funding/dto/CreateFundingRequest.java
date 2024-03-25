package com.zipup.server.funding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.global.util.entity.FundingPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateFundingRequest {
  private String title;
  private String roadAddress;
  private String detailAddress;
  private String phoneNumber;
  private String description;
  private Integer goalPrice;
  private String productUrl;
  private String imageUrl;
  private String fundingStart;
  private String fundingFinish;
  private String user;

  public Fund toEntity() {
    return Fund.builder()
            .title(title)
            .roadAddress(roadAddress)
            .detailAddress(detailAddress)
            .phoneNumber(phoneNumber)
            .description(description)
            .goalPrice(goalPrice)
            .productUrl(productUrl)
            .imageUrl(imageUrl)
            .fundingPeriod(new FundingPeriod(LocalDateTime.parse(fundingStart), LocalDateTime.parse(fundingFinish)))
            .build();
  }
}