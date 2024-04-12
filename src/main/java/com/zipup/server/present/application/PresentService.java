package com.zipup.server.present.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.present.infrastructure.PresentRepository;
import com.zipup.server.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;

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
      throw new BaseException(INVALID_USER_UUID);
    }
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

  @Transactional(readOnly = true)
  public List<PresentSummaryResponse> getParticipateList() {
    return presentRepository.findAll()
            .stream()
            .map(Present::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> getMyParticipateList(String userId) {
    isValidUUID(userId);
    return presentRepository.findAllByUser(userService.findById(userId))
            .stream()
            .map(present -> present.getFund().toSummaryResponse())
            .collect(Collectors.toList());
  }

}
