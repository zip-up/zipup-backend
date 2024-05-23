package com.zipup.server.user.facade;

import com.zipup.server.funding.dto.FundingCancelRequest;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.WithdrawalRequest;

import java.util.List;

public interface UserFacade<T> {
  User findUserById(String userId);
  SimpleDataResponse unlinkUser(WithdrawalRequest withdrawalRequest, String userId);
  List findMyEntityList(String userId);
  List deleteEntity(FundingCancelRequest request, String userId);
  FundingDetailResponse findEntityDetail(String entityId, String userId);
}