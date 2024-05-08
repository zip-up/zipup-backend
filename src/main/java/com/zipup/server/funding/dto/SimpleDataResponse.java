package com.zipup.server.funding.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class SimpleDataResponse {
  private final String id;
}
