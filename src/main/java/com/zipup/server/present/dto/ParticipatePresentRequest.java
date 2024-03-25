package com.zipup.server.present.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipatePresentRequest {
  private String participateId;
  private String fundingId;
  private String paymentId;
  private String senderName;
  private String congratsMessage;
}
