package com.zipup.server.present.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.domain.Payment;
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

@Service
@RequiredArgsConstructor
public class PresentService {
  private final UserService userService;
  private final FundService fundService;
  private final PaymentService paymentService;
  private final PresentRepository presentRepository;

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

  public List<PresentSummaryResponse> getParticipateList() {
    return presentRepository.findAll()
            .stream()
            .map(Present::toSummaryResponse)
            .collect(Collectors.toList());
  }

}
