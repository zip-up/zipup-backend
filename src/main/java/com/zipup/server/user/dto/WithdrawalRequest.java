package com.zipup.server.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalRequest {
  @Schema(description = "회원 id")
  private String userId;

  @Schema(description = "탈퇴 사유", required = true)
  private String withdrawalReason;
}
