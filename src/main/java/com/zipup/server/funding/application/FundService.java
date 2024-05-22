package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.*;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
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

  @Transactional(readOnly = true)
  public List<Fund> findAllByUserAndStatus(User user, ColumnStatus status) {
    return fundRepository.findAllByUserAndStatus(user, status);
  }

  @Transactional(readOnly = true)
  public List<FundingSummaryResponse> findFundingSummaryByUserIdAndStatus(String userId, ColumnStatus presentStatus, ColumnStatus fundStatus) {
    return fundRepository.findFundingSummaryByUserIdAndStatus(UUID.fromString(userId), presentStatus, fundStatus);
  }

  @Transactional
  public SimpleFundingDataResponse createFunding(CreateFundingRequest request) {
    String productUrl = request.getProductUrl();

    Fund targetFund = request.toEntity();
    targetFund.setUser(userService.findById(request.getUserId()));

    CrawlerResponse crawlerResponse = crawlerService.crawlingProductInfo(productUrl);
    String imageUrl = crawlerResponse == null ? ""
            : crawlerResponse.getImageUrl() == null ? ""
            : !crawlerResponse.getImageUrl().startsWith("https:")
            ? "https:" + crawlerResponse.getImageUrl() : crawlerResponse.getImageUrl();
    setImageUrl(targetFund, imageUrl);

    Fund response = fundRepository.save(targetFund);

    return new SimpleFundingDataResponse(response.getId().toString(), imageUrl);
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
    List<Fund> fundList = fundRepository.findAllByStatus(ColumnStatus.PUBLIC);
    return fundList.stream()
            .map(Fund::toSummaryResponse)
            .sorted((f1, f2) -> {
              int percentComparison = Double.compare(f2.getPercent(), f1.getPercent());
              if (percentComparison != 0) {
                return percentComparison;
              }
              boolean f1Positive = f1.getDDay() > 0;
              boolean f2Positive = f2.getDDay() > 0;
              if (f1Positive && f2Positive) return Long.compare(f1.getDDay(), f2.getDDay());
              if (f1Positive) return -1;
              else if (f2Positive) return 1;
              return Long.compare(f2.getDDay(), f1.getDDay());
            })
            .limit(10)
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
