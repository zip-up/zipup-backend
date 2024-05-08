package com.zipup.server.user.facade;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.UserException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.zipup.server.global.exception.CustomErrorCode.ACTIVE_FUNDING;

@Service
@RequiredArgsConstructor
public class UserFundFacade implements UserFacade<Fund> {

  private final UserService userService;
  private final FundService fundService;
  private final JwtProvider jwtProvider;

  @Override
  @Transactional(readOnly = true)
  public User findUserById(String id) {
    return userService.findById(id);
  }

  @Override
  public List<Fund> findAllEntityByUserAndStatus(User user, ColumnStatus status) {
    return fundService.findAllByUserAndStatus(user, status);
  }

  @Override
  @Transactional
  public SimpleDataResponse unlinkUser(String accessToken) {
    Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
    String userId = authentication.getName();
    User targetUser = userService.findById(userId);

    if (findAllEntityByUserAndStatus(targetUser, ColumnStatus.PUBLIC).size() > 0)
      throw new UserException(ACTIVE_FUNDING, userId);

    userService.unlinkStatusUser(targetUser);
    SecurityContextHolder.clearContext();

    return new SimpleDataResponse(userId);
  }

}
