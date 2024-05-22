package com.zipup.server.present.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class PresentSummaryResponse {

  @Schema(description = "참여 '내역' 식별자")
  private UUID id;
  @Schema(description = "참여자 이름, 축하 메시지 바로 위에 쓰는 From.")
  private String senderName;
  @Schema(description = "기여도")
  private int contributionPercent;
  @Schema(description = "참여자의 프로필 사진")
  private String profileImage;
  @Schema(description = "'참여자'의 식별자 값 (UUID) ")
  private UUID participantId;
  @Schema(description = "'참여 펀딩'의 식별자 값 (UUID) ")
  private UUID fundId;
  @Schema(description = "축하 메시지")
  private String congratsMessage;
  @Schema(description = "결제 정보 식별자")
  private UUID paymentId;

  public PresentSummaryResponse(UUID id, String senderName, int contributionPercent, String profileImage, UUID participantId, UUID fundId, String congratsMessage, UUID paymentId) {
    this.id = id;
    this.senderName = senderName;
    this.contributionPercent = contributionPercent;
    this.profileImage = profileImage;
    this.participantId = participantId;
    this.fundId = fundId;
    this.congratsMessage = congratsMessage;
    this.paymentId = paymentId;
  }

}
