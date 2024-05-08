package com.zipup.server.user.facade;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.UserException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.application.AuthService;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.ACTIVE_FUNDING;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class UserFundFacade implements UserFacade<Fund> {

  private final UserService userService;
  private final AuthService authService;
  private final FundService fundService;
  private final JwtProvider jwtProvider;

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
  public SimpleDataResponse unlinkUser(String accessToken) {
    String userId = getUserIdInToken(accessToken);
    User targetUser = userService.findById(userId);

    List<Fund> fundList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);
    List<FundingSummaryResponse> summaryList = fundList.stream()
            .map(Fund::toSummaryResponse)
            .filter(response -> response.getPercent() >= 100)
            .filter(response -> response.getStatus().equals("완료"))
            .collect(Collectors.toList());

    if (summaryList.size() > 0) throw new UserException(ACTIVE_FUNDING, userId);

    userService.unlinkStatusUser(targetUser);
    authService.removeIdInRedisToken(userId);

    SecurityContextHolder.clearContext();

    return new SimpleDataResponse(userId);
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> findMyEntityList(String accessToken) {
    String userId = getUserIdInToken(accessToken);
    User targetUser = findUserById(userId);
    List<Fund> fundList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    return fundList.stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

  private String getUserIdInToken(String accessToken) {
    Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
    String userId = authentication.getName();
    isValidUUID(userId);
    return userId;
  }

}
