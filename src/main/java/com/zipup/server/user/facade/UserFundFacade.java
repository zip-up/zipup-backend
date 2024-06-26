package com.zipup.server.user.facade;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingCancelRequest;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.*;
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
    User targetUser = userService.findById(userId);
    if (targetUser.getStatus().equals(ColumnStatus.UNLINK)) throw new BaseException(WITHDRAWAL_USER);
    return targetUser;
  }

  @Override
  @Transactional
  public SimpleDataResponse unlinkUser(WithdrawalRequest withdrawalRequest, String userId) {
    User targetUser = findUserById(userId);
    List<Fund> fundList = fundService.findAllByUserAndStatus(targetUser, ColumnStatus.PUBLIC);
    boolean hasActiveFunding = fundList.stream()
            .map(Fund::toSummaryResponse)
            .noneMatch(response -> response.getPercent() < 100 && response.getDDay() > 0);

    if (!hasActiveFunding) throw new UserException(ACTIVE_FUNDING, userId);

    fundList.forEach(fund -> {
      fundService.changeUnlinkFunding(fund);

      List<Present> presentList = fund.getPresents();
      presentList.forEach(presentService::changeUnlinkParticipate);
    });

    userService.setWithdrawalReason(targetUser, withdrawalRequest.getWithdrawalReason());
    userService.unlinkStatusUser(targetUser);
    authService.signOut(userId);

    return new SimpleDataResponse(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> findMyEntityList(String userId) {
    findUserById(userId);
    return fundService.findFundingSummaryByUserIdAndStatus(userId, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC);
  }

  @Override
  @Transactional
  public List<PresentSummaryResponse> deleteEntity(FundingCancelRequest request, String userId) {
    User targetUser = findUserById(userId);
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
  @Transactional(readOnly = true)
  public FundingDetailResponse findEntityDetail(String fundId, String userId) {
    isValidUUID(fundId);
    FundingDetailResponse response = fundService.findFundingDetailByFundIdAndStatus(fundId, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC);
    if (response == null) throw new ResourceNotFoundException(DATA_NOT_FOUND);

    List<PresentSummaryResponse> presentList = presentService.findPresentSummaryByFundIdAndStatus(fundId, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC);

    boolean isParticipant = presentList.stream()
            .filter(p -> userId != null)
            .anyMatch(p -> p.getParticipantId().equals(UUID.fromString(userId)));

    response.setIsOrganizer(userId != null && UUID.fromString(userId).equals(response.getOrganizer()));
    response.setIsParticipant(isParticipant);
    response.setIsCompleted(response.getExpirationDate() <= 0);
    response.setPresentList(presentList);
    return response;
  }

}
