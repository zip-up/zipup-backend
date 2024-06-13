package com.zipup.server.present.dto;

import com.zipup.server.payment.dto.VirtualAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class PresentSuccessResponse {

  @Schema(description = "참여 '내역' 식별자")
  private UUID id;
  @Schema(description = "결제 수단")
  private String method;
  @Schema(description = "가상 계좌 정보")
  private VirtualAccount virtualAccount;

}
