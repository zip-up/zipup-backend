package com.zipup.server.present.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipatePresentRequest {
  @Schema(description = "참여자 식별자")
  private String participateId;
  @Schema(description = "펀딩 식별자")
  private String fundingId;
  @Schema(description = "결제 정보 식별자")
  private String paymentId;
  @Schema(description = "참여자 이름, 축하 메시지 바로 위에 쓰는 From.")
  private String senderName;
  @Schema(description = "축하 메시지")
  private String congratsMessage;
}
