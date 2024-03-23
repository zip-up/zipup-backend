package com.zipup.server.funding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlerResponse {

  private String imageUrl;
  private String productName;
  private Integer price;

}
