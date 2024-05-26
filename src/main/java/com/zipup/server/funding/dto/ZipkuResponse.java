package com.zipup.server.funding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ZipkuResponse {
  private UUID id;
  private String title;
  private String productUrl;
  private String imageUrl;
  private int goalPrice;
}
