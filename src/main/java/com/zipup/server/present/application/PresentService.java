package com.zipup.server.present.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.PaymentException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.global.util.entity.PaymentStatus;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.payment.dto.PaymentHistoryResponse;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.ParticipateCancelRequest;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.present.infrastructure.PresentRepository;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class PresentService {
  private final UserService userService;
  private final FundService fundService;
  private final PaymentService paymentService;
  private final PresentRepository presentRepository;

  @Transactional(readOnly = true)
  public List<Present> findAllByUserAndStatus(User user, ColumnStatus status) {
    return presentRepository.findAllByUserAndStatus(user, status);
  }

  @Transactional(readOnly = true)
  public Present findByUserAndFund(User user, Fund fund) {
    return presentRepository.findByUserAndFund(user, fund)
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional
  public SimpleDataResponse participateFunding(ParticipatePresentRequest request) {
    String participateId = request.getParticipateId();
    String fundingId = request.getFundingId();
    String paymentId = request.getPaymentId();

    isValidUUID(fundingId);
    isValidUUID(paymentId);

    Present participateFunding = Present.builder()
            .fund(fundService.findById(fundingId))
            .user(userService.findById(participateId))
            .payment(paymentService.findById(paymentId))
            .senderName(request.getSenderName())
            .congratsMessage(request.getCongratsMessage())
            .build();

    return new SimpleDataResponse(presentRepository.save(participateFunding).getId().toString());
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
    User targetUser = userService.findById(userId);
    List<Present> presentList = findAllByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    presentList.forEach(present -> present.getFund().getId());

    return presentList.stream()
            .map(present -> present.getFund().toSummaryResponse())
            .collect(Collectors.toList());
  }

  @Transactional
  public String cancelParticipate(ParticipateCancelRequest request) {
    User targetUser = userService.findById(request.getParticipateId());
    Fund targetFunding = fundService.findById(request.getFundingId());
    Present targetPresent = findByUserAndFund(targetUser, targetFunding);

    if (!targetPresent.getUser().equals(targetUser)) throw new BaseException(ACCESS_DENIED);

    Payment targetPayment = targetPresent.getPayment();

    String paymentKey = targetPayment.getPaymentKey();
    Integer cancelAmount = request.getCancelAmount();

    if (cancelAmount != null && targetPayment.getBalanceAmount() < cancelAmount)
      throw new PaymentException(HttpStatus.FORBIDDEN.value(), "NOT_CANCELABLE_AMOUNT", "취소 할 수 없는 금액 입니다.");

    PaymentCancelRequest paymentCancelRequest = PaymentCancelRequest.builder()
            .paymentKey(paymentKey)
            .cancelReason(request.getCancelReason())
            .cancelAmount(cancelAmount)
            .refundReceiveAccount(request.getRefundReceiveAccount())
            .build();

    paymentService.cancelPayment(paymentCancelRequest);
    changePrivateParticipate(targetPresent);

    return "취소 성공";
  }

  @Transactional(readOnly = true)
  public List<PaymentHistoryResponse> getMyPaymentList(String userId) {
    User targetUser = userService.findById(userId);
    List<Present> presentList = findAllByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    return presentList.stream()
            .map(Present::getPayment)
            .map(this::createPaymentHistoryResponse)
            .collect(Collectors.toList());
  }

  private PaymentHistoryResponse createPaymentHistoryResponse(Payment payment) {
    Fund fund = payment.getPresent().getFund();
    FundingSummaryResponse response = fund.toSummaryResponse();
    boolean isVirtualAccountAndDepositCompleted = payment.getPaymentMethod().equals("가상계좌") && payment.getPaymentStatus().equals(PaymentStatus.DONE);
    boolean refundable = response.getPercent() < 100;

    return payment.toHistoryResponse(isVirtualAccountAndDepositCompleted, refundable);
  }

  @Transactional
  public void changePrivateParticipate(Present present) {
    present.setStatus(ColumnStatus.PRIVATE);
  }

  @Transactional
  public void changeUnlinkParticipate(Present present) {
    present.setStatus(ColumnStatus.UNLINK);
  }

}
