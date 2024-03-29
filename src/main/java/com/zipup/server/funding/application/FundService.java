package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.CreateFundingRequest;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;

@Service
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;
  private final UserService userService;

  private void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new BaseException(INVALID_USER_UUID);
    }
  }

  @Transactional(readOnly = true)
  public Fund findById(String id) {
    isValidUUID(id);
    return fundRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new BaseException(DATA_NOT_FOUND));
  }

  @Transactional
  public SimpleDataResponse createFunding(CreateFundingRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Fund targetFund = request.toEntity();
    targetFund.setUser(userService.findById(authentication.getName()));
    Fund response = fundRepository.save(targetFund);

    return new SimpleDataResponse(response.getId().toString());
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> getMyFundingList(String userId) {
    isValidUUID(userId);
    return fundRepository.findAllByUser(userService.findById(userId))
            .stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

  public FundingDetailResponse getFundingDetail(String fundId) {
    isValidUUID(fundId);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return findById(fundId).toDetailResponse(authentication.getName());
  }

  public List<FundingSummaryResponse> getFundList() {
    return fundRepository.findAll()
            .stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }
}
