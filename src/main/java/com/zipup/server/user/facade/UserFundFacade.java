package com.zipup.server.user.facade;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.UserException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.application.AuthService;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.WithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.ACTIVE_FUNDING;

@Service
@RequiredArgsConstructor
public class UserFundFacade implements UserFacade<Fund> {

  private final UserService userService;
  private final AuthService authService;
  private final FundService fundService;

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
  public SimpleDataResponse unlinkUser(WithdrawalRequest request) {
    String userId = request.getUserId();
    User targetUser = userService.findById(userId);

    List<Fund> fundList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);
    boolean hasActiveFunding = fundList.stream()
            .map(Fund::toSummaryResponse)
            .noneMatch(response -> response.getPercent() < 100 && !response.getStatus().equals("완료"));

//    if (!hasActiveFunding) throw new UserException(ACTIVE_FUNDING, userId);
    fundList.forEach(fund -> fund.setStatus(ColumnStatus.PRIVATE));
    targetUser.setWithdrawalReason(request.getWithdrawalReason());
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

}
