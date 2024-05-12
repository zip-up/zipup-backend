package com.zipup.server.user.facade;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.domain.Present;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.WithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPresentFacade implements UserFacade<Present> {
  private final UserService userService;
  private final PresentService presentService;

  @Override
  public User findUserById(String userId) {
    return userService.findById(userId);
  }

  @Override
  public List<Present> findAllEntityByUserAndStatus(User user, ColumnStatus status) {
    return presentService.findAllByUserAndStatus(user, status);
  }

  @Override
  public SimpleDataResponse unlinkUser(WithdrawalRequest accessToken) {
    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> findMyEntityList(String userId) {
    User targetUser = findUserById(userId);
    List<Present> presentList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    presentList.forEach(present -> present.getFund().getId());

    return presentList.stream()
            .map(present -> present.getFund().toSummaryResponse())
            .collect(Collectors.toList());
  }

}
