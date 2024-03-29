package com.zipup.server.funding.presentation;

import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.*;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fund")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Funding", description = "펀딩 주최 관련 API")
public class FundController {

  private final FundService fundService;
  private final CrawlerService crawlerService;

  @Operation(summary = "상품 이미지 크롤링", description = "상품 URL로 이미지 크롤링")
  @Parameter(name = "product", description = "상품 URL")
  @ApiResponse(
          responseCode = "200",
          description = "크롤링 성공",
          content = @Content(schema = @Schema(implementation = CrawlerResponse.class)))
  @GetMapping("/crawler")
  @Hidden
  public CrawlerResponse crawlingProductInfo(@RequestParam(value = "product") String url) {
    return crawlerService.crawlingProductInfo(url);
  }

  @Operation(summary = "내가 주최한 펀딩 목록 조회", description = "마이페이지에 있는 펀딩 목록")
  @Parameter(name = "user", description = "마이페이지 유저의 식별자 값 (UUID)")
  @ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = FundingSummaryResponse.class)))
  @GetMapping("/list")
  public ResponseEntity<List<FundingSummaryResponse>> getMyFundingList(@RequestParam(value = "user") String userId) {
    return ResponseEntity.ok(fundService.getMyFundingList(userId));
  }

  @Operation(summary = "펀딩 페이지 상세 조회", description = "펀딩 상세 내용")
  @Parameter(name = "funding", description = "선택한 펀딩의 식별자 값 (UUID)")
  @Parameter(name = "user", description = "진입한 유저의 식별자 값 (UUID)")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "조회 성공",
                  content = @Content(schema = @Schema(implementation = FundingDetailResponse.class))),
          @ApiResponse(
                  responseCode = "400",
                  description = "잘못된 UUID 형태",
                  content = @Content(schema = @Schema(type = "유효하지 않은 UUID입니다: {요청 인자}")))
  })
  @GetMapping("")
  public ResponseEntity<FundingDetailResponse> getFundingDetail(
          @RequestParam(value = "funding") String fundId,
          @RequestParam(value = "user", required = false) String userId
  ) {
    return ResponseEntity.ok(fundService.getFundingDetail(fundId));
  }

  @Operation(summary = "펀딩 주최", description = "펀딩 주최")
  @ApiResponse(
          responseCode = "201",
          description = "펀딩 주최 성공",
          content = @Content(schema = @Schema(implementation = SimpleDataResponse.class)))
  @PostMapping("")
  public ResponseEntity<SimpleDataResponse> createFunding(@RequestBody CreateFundingRequest request) {
    return ResponseEntity.ok(fundService.createFunding(request));
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<FundingSummaryResponse>> getFundList() {
    List<FundingSummaryResponse> response = fundService.getFundList();
    return ResponseEntity.ok(response);
  }

}
