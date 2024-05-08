package com.zipup.server.user.facade;

import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.domain.Present;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class UserPresentFacade implements UserFacade<Present> {
  private final UserService userService;
  private final PresentService presentService;
  private final JwtProvider jwtProvider;

  @Override
  public User findUserById(String userId) {
    return userService.findById(userId);
  }

  @Override
  public List<Present> findAllEntityByUserAndStatus(User user, ColumnStatus status) {
    return presentService.findAllByUserAndStatus(user, status);
  }

  @Override
  public SimpleDataResponse unlinkUser(String accessToken) {
    return null;
  }

  @Override
  public List<FundingSummaryResponse> findMyEntityList(String accessToken) {
    String userId = getUserIdInToken(accessToken);
    User targetUser = findUserById(userId);
    List<Present> presentList = findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC);

    return presentList.stream()
            .map(present -> present.getFund().toSummaryResponse())
            .collect(Collectors.toList());
  }

  private String getUserIdInToken(String accessToken) {
    Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
    String userId = authentication.getName();
    isValidUUID(userId);
    return userId;
  }

}
