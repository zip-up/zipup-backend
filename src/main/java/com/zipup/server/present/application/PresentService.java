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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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

  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Transactional(readOnly = true)
  public Present findById(String id) {
    isValidUUID(id);
    return presentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Present findByPayment(Payment payment) {
    return presentRepository.findByPayment(payment)
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public List<Present> findAllByUserAndStatus(User user, ColumnStatus status) {
    return presentRepository.findAllByUserAndStatus(user, status);
  }

  @Transactional
  public void changePrivateParticipate(Present present) {
    present.setStatus(ColumnStatus.PRIVATE);
  }

  @Transactional
  public void changeUnlinkParticipate(Present present) {
    present.setStatus(ColumnStatus.UNLINK);
    paymentService.changeUnlinkPayment(present.getPayment());
  }

  @Transactional
  public void setPresentCancelReason(Present present, String cancelReason) {
    present.setCancelReason(cancelReason);
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
            .map(Present::getFund)
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public String cancelParticipate(ParticipateCancelRequest request) {
    User targetUser = userService.findById(request.getUserId());
    Payment targetPayment = paymentService.findById(request.getPaymentId());
    Present targetPresent = findByPayment(targetPayment);

    if (!targetPresent.getUser().equals(targetUser)) throw new BaseException(ACCESS_DENIED);

    String paymentKey = targetPayment.getPaymentKey();
    Integer cancelAmount = request.getCancelAmount();

    if (cancelAmount != null && targetPayment.getBalanceAmount() < cancelAmount)
      throw new PaymentException(HttpStatus.FORBIDDEN.value(), "NOT_CANCELABLE_AMOUNT", "취소 할 수 없는 금액 입니다.");

    PaymentCancelRequest paymentCancelRequest = PaymentCancelRequest.builder()
            .userId(request.getUserId())
            .paymentKey(paymentKey)
            .cancelReason(request.getCancelReason())
            .cancelAmount(cancelAmount)
            .refundReceiveAccount(request.getRefundReceiveAccount())
            .build();

    paymentService.cancelPayment(paymentCancelRequest);
    changePrivateParticipate(targetPresent);
    setPresentCancelReason(targetPresent, request.getCancelReason());

    return "취소 성공";
  }

  @Transactional(readOnly = true)
  public List<PaymentHistoryResponse> getMyPaymentList(String userId) {
    User targetUser = userService.findById(userId);
    List<Present> presentList = findAllByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    return presentList.stream()
            .map(this::createPaymentHistoryResponse)
            .sorted(paymentHistoryResponseComparator())
            .collect(Collectors.toList());
  }

  private PaymentHistoryResponse createPaymentHistoryResponse(Present present) {
    Payment targetPayment = present.getPayment();
    Fund targetFund = present.getFund();
    FundingSummaryResponse response = targetFund.toSummaryResponse();
    boolean refundable = response.getPercent() < 100;

    LocalDateTime mostRecentPaymentDateInFunding = targetFund.getPresents().stream()
            .map(Present::getCreatedDate)
            .max(Comparator.naturalOrder())
            .orElse(present.getCreatedDate());

    return toHistoryResponse(targetPayment, present, refundable, mostRecentPaymentDateInFunding);
  }

  private PaymentHistoryResponse toHistoryResponse(Payment payment, Present present, Boolean refundable, LocalDateTime mostRecentPaymentDateInFunding) {
    String historyStatus = getStatusText(payment.getPaymentStatus());
    String paymentNumber = payment.getId().toString().replaceAll("-", "");
    boolean isVirtualAccount = payment.getPaymentMethod().equals("가상계좌");
    boolean isDepositCompleted = payment.getPaymentStatus().equals(PaymentStatus.DONE);

    return PaymentHistoryResponse.builder()
            .id(payment.getId().toString())
            .fundingName(present.getFund().getTitle())
            .fundingImage(present.getFund().getImageUrl())
            .paymentDate(payment.getCreatedDate().format(FORMATTER))
            .mostRecentPaymentDateInFunding(mostRecentPaymentDateInFunding.format(FORMATTER))
            .status(historyStatus)
            .amount(payment.getBalanceAmount())
            .paymentNumber(paymentNumber.substring(0, Math.min(15, paymentNumber.length())))
            .refundable(refundable)
            .isVirtualAccount(isVirtualAccount)
            .isDepositCompleted(isDepositCompleted)
            .build();
  }

  private Comparator<PaymentHistoryResponse> paymentHistoryResponseComparator() {
    return Comparator
            .comparing((PaymentHistoryResponse r) -> LocalDateTime.parse(r.getMostRecentPaymentDateInFunding(), FORMATTER))
            .thenComparing((PaymentHistoryResponse r) -> LocalDateTime.parse(r.getPaymentDate(), FORMATTER))
            .reversed();
  }

  private String getStatusText(PaymentStatus status) {
    switch (status) {
      case DONE:
      case READY:
      case WAITING_FOR_DEPOSIT:
        return "결제완료";
      case CANCELED:
      case PARTIAL_CANCELED:
        return "취소완료";
      default:
        return "취소요청";
    }
  }

}
