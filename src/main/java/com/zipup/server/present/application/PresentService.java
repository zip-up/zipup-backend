package com.zipup.server.present.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.infrastructure.PresentRepository;
import com.zipup.server.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresentService {
  private final UserService userService;
  private final FundService fundService;
  private final PaymentService paymentService;
  private final PresentRepository presentRepository;

  private void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 UUID입니다: " + id);
    }
  }

  public FundingDetailResponse getFundingDetail(String id) {
    isValidUUID(id);
    return fundService.getFundingDetail(id);
  }

  @Transactional
  public String participateFunding(ParticipatePresentRequest request) {
    String fundingId = request.getFundingId();
    String participateId = request.getParticipateId();
    String paymentId = request.getPaymentId();

    UUID.fromString(fundingId);
    UUID.fromString(participateId);

    Present participateFunding = Present.builder()
            .fund(fundService.findById(fundingId))
            .user(userService.findById(participateId))
            .payment(paymentService.findById(paymentId))
            .senderName(request.getSenderName())
            .congratsMessage(request.getCongratsMessage())
            .build();

    presentRepository.save(participateFunding);
    return participateFunding.getId().toString();
  }

}
