package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.CreateFundingRequest;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;
  private final UserService userService;

  private void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 UUID입니다: " + id);
    }
  }

  @Transactional(readOnly = true)
  public Fund findById(String id) {
    isValidUUID(id);
    return fundRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new NoResultException("존재하지 않는 펀딩이에요."));
  }

  @Transactional
  public SimpleDataResponse createFunding(CreateFundingRequest request) {
    User user = userService.findById(request.getUser());
    Fund targetFund = request.toEntity();
    targetFund.setUser(user);

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

  public FundingDetailResponse getFundingDetail(String id) {
    isValidUUID(id);
    return findById(id).toDetailResponse();
  }

}
