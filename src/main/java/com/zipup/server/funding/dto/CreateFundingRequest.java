package com.zipup.server.funding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.global.util.entity.FundingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateFundingRequest {
  @Schema(description = "펀딩 제목")
  private String title;
  @Schema(description = "주소")
  private String roadAddress;
  @Schema(description = "상세 주소")
  private String detailAddress;
  @Schema(description = "전화 번호")
  private String phoneNumber;
  @Schema(description = "설명")
  private String description;
  @Schema(description = "목표 금액")
  private Integer goalPrice;
  @Schema(description = "상품 url")
  private String productUrl;
  @Schema(description = "이미지 url, 기본 이미지 필요할 듯!")
  private String imageUrl;
  @Schema(description = "펀딩 시작")
  private String fundingStart;
  @Schema(description = "펀딩 종료")
  private String fundingFinish;

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