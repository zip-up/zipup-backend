package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.CreateFundingRequest;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;
  private final UserService userService;

  @Transactional
  public SimpleDataResponse createFunding(CreateFundingRequest request) {
    User user = userService.findById(request.getUser());
    Fund targetFund = request.toEntity();
    targetFund.setUser(user);

    Fund response = fundRepository.save(targetFund);

    return new SimpleDataResponse(response.getId().toString());
  }

}
