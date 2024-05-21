package com.zipup.server.user.facade;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingCancelRequest;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.UserException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.user.application.AuthService;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.WithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.zipup.server.global.exception.CustomErrorCode.ACTIVE_FUNDING;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class UserFundFacade implements UserFacade<Fund> {

  private final UserService userService;
  private final AuthService authService;
  private final FundService fundService;
  private final PresentService presentService;

  @Override
  @Transactional(readOnly = true)
  public User findUserById(String userId) {
    return userService.findById(userId);
  }

  @Override
  public List<Fund> findAllEntityByUserAndStatus(User user, ColumnStatus status) {
    return fundService.findAllByUserAndStatus(user, status);
  }

  @Override
  @Transactional
  public SimpleDataResponse unlinkUser(WithdrawalRequest withdrawalRequest) {
    String userId = withdrawalRequest.getUserId();
    User targetUser = userService.findById(userId);

    List<Fund> fundList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);
    boolean hasActiveFunding = fundList.stream()
            .map(Fund::toSummaryResponse)
            .noneMatch(response -> response.getPercent() < 100 && !response.getStatus().equals("완료"));

    if (!hasActiveFunding) throw new UserException(ACTIVE_FUNDING, userId);

    fundList.forEach(fund -> {
      fundService.changeUnlinkFunding(fund);

      List<Present> presentList = fund.getPresents();
      presentList.forEach(presentService::changeUnlinkParticipate);
    });

    userService.setWithdrawalReason(targetUser, withdrawalRequest.getWithdrawalReason());
    userService.unlinkStatusUser(targetUser);
    authService.removeIdInRedisToken(userId);

    SecurityContextHolder.clearContext();

    return new SimpleDataResponse(userId);
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> findMyEntityList(String userId) {
    User targetUser = findUserById(userId);
    List<Fund> fundList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    return fundList.stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public List<PresentSummaryResponse> deleteEntity(FundingCancelRequest request) {
    User targetUser = findUserById(request.getUserId());
    Fund targetFund = fundService.findById(request.getFundingId());

    if (!targetFund.getUser().equals(targetUser)) throw new BaseException(ACCESS_DENIED);

    fundService.setFundCancelReason(targetFund, request.getCancelReason());
    fundService.changePrivateFunding(targetFund);

    List<Present> presentList = targetFund.getPresents();
    presentList.forEach(presentService::changePrivateParticipate);

    return presentList.stream()
            .map(Present::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Override
  public FundingDetailResponse findEntityDetail(String fundId, String userId) {
    isValidUUID(fundId);
    Fund targetFund = fundService.findById(fundId);
    User targetUser = userService.findById(userId);
    List<Present> targetPresent = presentService.findAllByFundAndStatus(targetFund, ColumnStatus.PUBLIC);

    Boolean isOrganizer = targetUser != null && targetUser.equals(targetFund.getUser());
    boolean isParticipant = targetPresent.stream()
            .anyMatch(p -> p.getUser().equals(targetUser));
    int nowPresent = targetPresent.stream()
            .mapToInt(present -> present.getPayment().getBalanceAmount())
            .sum();
    int goalPrice = targetFund.getGoalPrice();
    long duration = Duration.between(LocalDateTime.now(), targetFund.getFundingPeriod().getFinishFunding()).toDays();
    String fundingStatus = duration > 0 ? String.valueOf(duration) : "완료";

    return FundingDetailResponse.builder()
            .id(targetFund.getId().toString())
            .title(targetFund.getTitle())
            .imageUrl(targetFund.getImageUrl())
            .productUrl(targetFund.getProductUrl())
            .description(targetFund.getDescription())
            .expirationDate(fundingStatus.equals("완료") ? 0 : Long.parseLong(fundingStatus))
            .isCompleted(fundingStatus.equals("완료"))
            .goalPrice(targetFund.getGoalPrice())
            .percent((int) Math.round(((double) nowPresent / goalPrice) * 100))
            .presentList(targetPresent.stream().map(Present::toSummaryResponse).collect(Collectors.toList()))
            .isOrganizer(isOrganizer)
            .isParticipant(isParticipant)
            .organizer(targetUser.getId().toString())
            .organizerName(targetUser.getName())
            .build();
  }

}
