package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.*;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.security.util.AuthenticationUtil;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.security.util.AuthenticationUtil.getZipupAuthentication;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;
  private final UserService userService;
  private final CrawlerService crawlerService;

  @Transactional(readOnly = true)
  public Fund findById(String id) {
    isValidUUID(id);
    return fundRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional
  public SimpleFundingDataResponse createFunding(CreateFundingRequest request) {
    Authentication authentication = getZipupAuthentication();
    String productUrl = request.getProductUrl();

    Fund targetFund = request.toEntity();
    targetFund.setUser(userService.findById(authentication.getName()));
    CrawlerResponse crawlerResponse = crawlerService.crawlingProductInfo(productUrl);
    String imageUrl = crawlerResponse == null ? ""
            : crawlerResponse.getImageUrl() == null ? ""
            : !crawlerResponse.getImageUrl().startsWith("https:")
            ? "https:" + crawlerResponse.getImageUrl() : crawlerResponse.getImageUrl();
    targetFund.setImageUrl(imageUrl);

    Fund response = fundRepository.save(targetFund);

    return new SimpleFundingDataResponse(response.getId().toString(), imageUrl);
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> getMyFundingList(String userId) {
    if (userId == null || userId.isEmpty()) userId = getZipupAuthentication().getName();
    isValidUUID(userId);
    return fundRepository.findAllByUserAndStatus(userService.findById(userId), ColumnStatus.PUBLIC)
            .stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public FundingDetailResponse getFundingDetail(String fundId) {
    isValidUUID(fundId);
    Authentication authentication = getZipupAuthentication();

    return findById(fundId).toDetailResponse(authentication.getName());
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> getFundList() {
    return fundRepository.findAll()
            .stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

}
