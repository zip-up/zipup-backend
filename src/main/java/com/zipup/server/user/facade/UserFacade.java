package com.zipup.server.user.facade;

import com.zipup.server.funding.dto.FundingCancelRequest;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.WithdrawalRequest;

import java.util.List;

public interface UserFacade<T> {
  User findUserById(String userId);
  List<T> findAllEntityByUserAndStatus(User user, ColumnStatus status);
  SimpleDataResponse unlinkUser(WithdrawalRequest userId);
  List findMyEntityList(String userId);
  List deleteEntity(FundingCancelRequest request);
}