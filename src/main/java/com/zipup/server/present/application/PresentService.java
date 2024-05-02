package com.zipup.server.present.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.PaymentException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.ParticipateCancelRequest;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.present.infrastructure.PresentRepository;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.security.util.AuthenticationUtil.getZipupAuthentication;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class PresentService {
  private final UserService userService;
  private final FundService fundService;
  private final PaymentService paymentService;
  private final PresentRepository presentRepository;

  @Transactional(readOnly = true)
  public Present findByUserAndFund(User user, Fund fund) {
    return presentRepository.findByUserAndFund(user, fund)
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional
  public SimpleDataResponse participateFunding(ParticipatePresentRequest request) {
    Authentication authentication = getZipupAuthentication();
    String participateId = authentication.getName();

    String fundingId = request.getFundingId();
    String paymentId = request.getPaymentId();

    isValidUUID(fundingId);
    isValidUUID(participateId);

    Present participateFunding = Present.builder()
            .fund(fundService.findById(fundingId))
            .user(userService.findById(participateId))
            .payment(paymentService.findById(paymentId))
            .senderName(request.getSenderName())
            .congratsMessage(request.getCongratsMessage())
            .build();

    presentRepository.save(participateFunding);
    return new SimpleDataResponse(participateFunding.getId().toString());
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
    return presentRepository.findAllByUserAndStatus(userService.findById(userId), ColumnStatus.PUBLIC)
            .stream()
            .map(present -> present.getFund().toSummaryResponse())
            .collect(Collectors.toList());
  }

  @Transactional
  public String cancelParticipate(ParticipateCancelRequest request) {
    Authentication authentication = getZipupAuthentication();
    User targetUser = userService.findById(authentication.getName());
    Fund targetFunding = fundService.findById(request.getFundingId());
    Present targetPresent = findByUserAndFund(targetUser, targetFunding);

    if (!targetPresent.getUser().getId().toString().equals(authentication.getName())) throw new BaseException(ACCESS_DENIED);

    Payment targetPayment = targetPresent.getPayment();

    String paymentKey = targetPayment.getPaymentKey();
    Integer cancelAmount = request.getCancelAmount();

    if (cancelAmount != null && targetPayment.getBalanceAmount() < cancelAmount)
      throw new PaymentException(403, "NOT_CANCELABLE_AMOUNT", "취소 할 수 없는 금액 입니다.");

    PaymentCancelRequest paymentCancelRequest = PaymentCancelRequest.builder()
            .paymentKey(paymentKey)
            .cancelReason(request.getCancelReason())
            .cancelAmount(cancelAmount)
            .refundReceiveAccount(request.getRefundReceiveAccount())
            .build();

    paymentService.cancelPayment(paymentCancelRequest);
    changeStatusParticipate(targetPresent);

    return "취소 성공";
  }

  @Transactional
  public void changeStatusParticipate(Present present) {
    present.setStatus(ColumnStatus.PRIVATE);
  }

}
