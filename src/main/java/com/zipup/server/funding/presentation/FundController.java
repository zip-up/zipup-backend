package com.zipup.server.funding.presentation;

import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.*;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.user.facade.UserFacade;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_ACCESS_JWT;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@RestController
@RequestMapping("/api/v1/fund")
@Slf4j
@Tag(name = "Funding", description = "펀딩 주최 관련 API")
public class FundController {

  private final FundService fundService;
  private final CrawlerService crawlerService;
  private final JwtProvider jwtProvider;
  @SuppressWarnings("rawtypes")
  private final UserFacade userFacade;

  @SuppressWarnings("rawtypes")
  public FundController(FundService fundService, @Qualifier("userFundFacade") UserFacade userFacade, JwtProvider jwtProvider, CrawlerService crawlerService) {
    this.fundService = fundService;
    this.userFacade = userFacade;
    this.crawlerService = crawlerService;
    this.jwtProvider = jwtProvider;
  }

  @Operation(summary = "상품 이미지 크롤링", description = "상품 URL로 이미지 크롤링")
  @Parameter(name = "product", description = "상품 URL")
  @ApiResponse(
          responseCode = "200",
          description = "크롤링 성공",
          content = @Content(schema = @Schema(implementation = CrawlerResponse.class)))
  @GetMapping("/crawler")
  @Hidden
  public CrawlerResponse crawlingProductInfo(
          @RequestParam(value = "product") String url
  ) {
    return crawlerService.crawlingProductInfo(url);
  }

  @Operation(summary = "내가 주최한 펀딩 목록 조회", description = "마이페이지에 있는 펀딩 목록")
  @ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = FundingSummaryResponse.class)))
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/list")
  public List<FundingSummaryResponse> getMyFundingList(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user
  ) {
    return (List<FundingSummaryResponse>) userFacade.findMyEntityList(user.getUsername());
  }

  @Operation(summary = "펀딩 페이지 상세 조회", description = "펀딩 상세 내용")
  @Parameter(name = "funding", description = "선택한 펀딩의 식별자 값 (UUID)")
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
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("")
  public FundingDetailResponse getFundingDetail(
          final HttpServletRequest httpServletRequest,
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestParam(value = "funding") String fundId
  ) {
    String accessToken = jwtProvider.resolveToken(httpServletRequest);
    String userId = null;
    if (accessToken != null) {
      if (!StringUtils.hasText(accessToken)) throw new BaseException(EMPTY_ACCESS_JWT);
      Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
      userId = authentication.getName();
      isValidUUID(userId);
    }

    return userFacade.findEntityDetail(fundId, userId);
  }

  @Operation(summary = "펀딩 주최", description = "펀딩 주최")
  @ApiResponse(
          responseCode = "201",
          description = "펀딩 주최 성공",
          content = @Content(schema = @Schema(implementation = SimpleFundingDataResponse.class)))
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  public SimpleFundingDataResponse createFunding(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestBody CreateFundingRequest request
  ) {
    return fundService.createFunding(request, user.getUsername());
  }

  @Operation(summary = "펀딩 주최 - 요즘 핫한 집꾸템 추천!", description = "펀딩 주최 - 요즘 핫한 집꾸템 추천!")
  @ApiResponse(
          responseCode = "201",
          description = "펀딩 주최 성공",
          content = @Content(schema = @Schema(implementation = SimpleFundingDataResponse.class)))
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/static")
  public SimpleFundingDataResponse createStaticFunding(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestBody CreateFundingRequest request
  ) {
    return fundService.createStaticFunding(request, user.getUsername());
  }

  @Operation(summary = "주최자 - 펀딩 삭제", description = "주최자 - 펀딩 삭제")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "펀딩 취소 요청. 주최자 id는 비워서 요청하시면 됩니다.")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "펀딩 삭제 성공",
                  content = @Content(schema = @Schema(type = "펀딩 삭제 성공", implementation = PresentSummaryResponse.class)))
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/cancel")
  public List<PresentSummaryResponse> cancelFunding(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestBody FundingCancelRequest request
  ) {
    return (List<PresentSummaryResponse>) userFacade.deleteEntity(request, user.getUsername());
  }

  @Operation(summary = "인기 펀딩 목록 api", description = "참여도 퍼센트 높은 펀딩 순 + 진행 중인 펀딩 + 기간이 적게 남은 펀딩")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "인기 펀딩 조회 성공",
                  content = @Content(schema = @Schema(type = "인기 펀딩 조회 성공", implementation = FundingSummaryResponse.class)))
  })
  @GetMapping("/popular")
  public List<FundingSummaryResponse> getPopularFundingList() {
    return fundService.getPopularFundingList();
  }

  @Operation(summary = "펀딩 목록 api - 요즘 핫한 집꾸템 추천!", description = "정렬 기준 없음")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "집꾸템 조회 성공",
                  content = @Content(schema = @Schema(type = "집꾸템 조회 성공", implementation = FundingSummaryResponse.class)))
  })
  @GetMapping("/static")
  public List<ZipkuResponse> getStaticFundingList() {
    return fundService.getStaticFundingList();
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<FundingSummaryResponse>> getFundList() {
    List<FundingSummaryResponse> response = fundService.getFundList();
    return ResponseEntity.ok().body(response);
  }

}
