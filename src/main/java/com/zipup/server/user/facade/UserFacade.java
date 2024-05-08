package com.zipup.server.user.facade;

import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.domain.User;

import java.util.List;

public interface UserFacade<T> {
  User findUserById(String userId);
  List<T> findAllEntityByUserAndStatus(User user, ColumnStatus status);
  SimpleDataResponse unlinkUser(String accessToken);
  List findMyEntityList(String accessToken);
}