package com.zipup.server.present.presentation;

import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.payment.dto.PaymentHistoryResponse;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.dto.ParticipateCancelRequest;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.dto.PresentSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/present")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Present", description = "펀딩 참여 관련 API")
public class PresentController {

  private final PresentService presentService;

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
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("")
  public SimpleDataResponse participateFunding(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestBody ParticipatePresentRequest request
  ) {
    request.setParticipateId(user.getUsername());
    return presentService.participateFunding(request);
  }

  @Operation(summary = "펀딩 참여 취소", description = "펀딩 결제 취소 - 참여자")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "참여자의 펀딩 결제 요청")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "참여 취소 성공",
                  content = @Content(schema = @Schema(type = "취소 성공")))
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/cancel")
  public String cancelParticipate(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          @RequestBody ParticipateCancelRequest request
  ) {
    request.setUserId(user.getUsername());
    return presentService.cancelParticipate(request);
  }

  @Operation(summary = "내가 참여한 펀딩 목록 조회",
          description = "마이페이지에 있는 펀딩 목록",
          security = @SecurityRequirement(name = "JWT-Auth"))
  @ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = FundingSummaryResponse.class)))
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/list")
  public List<FundingSummaryResponse> getMyParticipateList(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user
  ) {
    return presentService.getMyParticipateList(user.getUsername());
  }

  @Operation(summary = "내 결제 내역 조회", description = "내 정보 관리 > 결제 내역")
  @ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PaymentHistoryResponse.class)))
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/payment/list")
  public List<PaymentHistoryResponse> getMyPaymentList(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user
  ) {
    return presentService.getMyPaymentList(user.getUsername());
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/temp")
  public List<PresentSummaryResponse> getParticipateList() {
    return presentService.getParticipateList();
  }

}
