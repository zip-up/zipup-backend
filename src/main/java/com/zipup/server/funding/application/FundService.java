package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.domain.Zipku;
import com.zipup.server.funding.dto.*;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.funding.infrastructure.ZipkuRepository;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.exception.CustomErrorCode.WITHDRAWAL_USER;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;
  private final ZipkuRepository zipkuRepository;
  private final UserService userService;
  private final CrawlerService crawlerService;

  @Transactional(readOnly = true)
  public Fund findById(String id) {
    isValidUUID(id);
    return fundRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public List<Fund> findAllByUserAndStatus(User user, ColumnStatus status) {
    return fundRepository.findAllByUserAndStatus(user, status);
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> findFundingSummaryByUserIdAndStatus(String userId, ColumnStatus userStatus, ColumnStatus presentStatus, ColumnStatus fundStatus) {
    return fundRepository.findFundingSummaryByUserIdAndStatus(UUID.fromString(userId), userStatus, presentStatus, fundStatus);
  }

  @Transactional(readOnly = true)
  public FundingDetailResponse findFundingDetailByFundIdAndStatus(String fundId, ColumnStatus userStatus, ColumnStatus presentStatus, ColumnStatus fundStatus) {
    return fundRepository.findFundingDetailByFundIdAndStatus(UUID.fromString(fundId), userStatus, presentStatus, fundStatus);
  }
  @Transactional(readOnly = true)
  public List<FundingAllResponse> findFundingDetailAll(ColumnStatus userStatus, ColumnStatus presentStatus, ColumnStatus fundStatus) {
    return fundRepository.findFundingDetailAll(userStatus, presentStatus, fundStatus);
  }

  @Transactional
  public SimpleFundingDataResponse createFunding(CreateFundingRequest request, String userId) {
    String productUrl = request.getProductUrl();

    Fund targetFund = request.toEntity();
    User targetUser = userService.findById(userId);
    if (targetUser.getStatus().equals(ColumnStatus.UNLINK)) throw new BaseException(WITHDRAWAL_USER);
    targetFund.setUser(targetUser);

    CrawlerResponse crawlerResponse = crawlerService.crawlingProductInfo(productUrl);
    String imageUrl = crawlerResponse == null ? ""
            : crawlerResponse.getImageUrl() == null ? ""
            : !crawlerResponse.getImageUrl().startsWith("https:")
            ? "https:" + crawlerResponse.getImageUrl() : crawlerResponse.getImageUrl();
    setImageUrl(targetFund, imageUrl);

    Fund response = fundRepository.save(targetFund);

    return new SimpleFundingDataResponse(response.getId().toString(), imageUrl);
  }

  @Transactional
  public SimpleFundingDataResponse createStaticFunding(CreateFundingRequest request, String userId) {
    Fund targetFund = request.toEntity();
    User targetUser = userService.findById(userId);
    if (targetUser.getStatus().equals(ColumnStatus.UNLINK)) throw new BaseException(WITHDRAWAL_USER);
    targetFund.setUser(targetUser);
    setImageUrl(targetFund, request.getImageUrl());
    Fund response = fundRepository.save(targetFund);

    return new SimpleFundingDataResponse(response.getId().toString(), request.getImageUrl());
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> getFundList() {
    return fundRepository.findAll()
            .stream()
            .map(Fund::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> getPopularFundingList() {
    return fundRepository.findPopularFundingSummaryByStatus(ColumnStatus.PUBLIC, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC, PageRequest.of(0, 10));
  }

  @Transactional(readOnly = true)
  public List<ZipkuResponse> getStaticFundingList() {
    return zipkuRepository.findAll()
            .stream()
            .map(Zipku::toSummaryResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public void changePrivateFunding(Fund fund) {
    fund.setStatus(ColumnStatus.PRIVATE);
  }

  @Transactional
  public void changeUnlinkFunding(Fund fund) {
    fund.setStatus(ColumnStatus.UNLINK);
  }

  @Transactional
  public void setFundCancelReason(Fund fund, String cancelReason) {
    fund.setCancelReason(cancelReason);
  }

  @Transactional
  public void setImageUrl(Fund fund, String imageUrl) {
    fund.setImageUrl(imageUrl);
  }

}
