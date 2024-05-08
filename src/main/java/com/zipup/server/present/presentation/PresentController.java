package com.zipup.server.present.presentation;

import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.dto.ParticipateCancelRequest;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.user.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_ACCESS_JWT;

@RestController
@RequestMapping("/api/v1/present")
@Slf4j
@Tag(name = "Present", description = "펀딩 참여 관련 API")
public class PresentController {
  private final PresentService presentService;
  @Qualifier("userPresentFacade")
  private final UserFacade userFacade;
  private final JwtProvider jwtProvider;

  public PresentController(PresentService presentService, @Qualifier("userPresentFacade") UserFacade userFacade, JwtProvider jwtProvider) {
    this.presentService = presentService;
    this.userFacade = userFacade;
    this.jwtProvider = jwtProvider;
  }

  @Operation(summary = "펀딩 참여하기", description = "펀딩 식별자 값으로 참여할 펀딩 조회")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "펀딩, 참여자, 결제 내역에 대한 식별자 값과 보내는 사람 이름 & 축하 메시지")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "참여 성공",
                  content = @Content(schema = @Schema(implementation = SimpleDataResponse.class))),
          @ApiResponse(
                  responseCode = "400",
                  description = "잘못된 UUID 형태",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("")
  public ResponseEntity<SimpleDataResponse> participateFunding(@RequestBody ParticipatePresentRequest request) {
    return ResponseEntity.ok(presentService.participateFunding(request));
  }

  @Operation(summary = "펀딩 참여 취소", description = "펀딩 결제 취소 - 참여자")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "참여자의 펀딩 결제 요청")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "참여 취소 성공",
                  content = @Content(schema = @Schema(type = "취소 성공")))
  })
  @PutMapping("/cancel")
  public ResponseEntity<String> cancelParticipate(@RequestBody ParticipateCancelRequest request) {
    return ResponseEntity.ok(presentService.cancelParticipate(request));
  }

//  @Operation(summary = "[deprecated] 내가 참여한 펀딩 목록 조회", description = "토큰 방식 조회가 문제 없으면 이 api는 삭제할 예정입니다.")
//  @ApiResponse(
//          responseCode = "200",
//          description = "조회 성공",
//          content = @Content(schema = @Schema(implementation = FundingSummaryResponse.class)))
//  @GetMapping("/list")
//  public ResponseEntity<List<FundingSummaryResponse>> getMyParticipateList(@RequestParam(value = "user", required = false) String userId) {
//    return ResponseEntity.ok(presentService.getMyParticipateList(userId));
//  }

  @Operation(summary = "내가 참여한 펀딩 목록 조회", description = "마이페이지에 있는 펀딩 목록")
  @ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = FundingSummaryResponse.class)))
  @GetMapping("/list")
  public ResponseEntity<List<FundingSummaryResponse>> getMyParticipateList(
          final HttpServletRequest request,
          final HttpServletResponse response
  ) {
    String accessToken = jwtProvider.resolveToken(request);
    if (!StringUtils.hasText(accessToken)) throw new BaseException(EMPTY_ACCESS_JWT);
    List<FundingSummaryResponse> myFundingList = userFacade.findMyEntityList(accessToken);

    return ResponseEntity.ok(myFundingList);
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<PresentSummaryResponse>> getParticipateList() {
    List<PresentSummaryResponse> response = presentService.getParticipateList();
    return ResponseEntity.ok(response);
  }
}
