package com.zipup.server.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PickUpInfoRequest {
  @Schema(description = "주소", required = true)
  private String roadAddress;
  @Schema(description = "상세 주소", required = true)
  private String detailAddress;
  @Schema(description = "전화 번호", required = true)
  private String phoneNumber;
}
