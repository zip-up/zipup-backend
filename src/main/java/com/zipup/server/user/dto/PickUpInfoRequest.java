package com.zipup.server.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickUpInfoRequest {
  @Schema(description = "주소", required = true)
  private String roadAddress;
  @Schema(description = "상세 주소", required = true)
  private String detailAddress;
  @Schema(description = "전화 번호", required = true)
  private String phoneNumber;
}
