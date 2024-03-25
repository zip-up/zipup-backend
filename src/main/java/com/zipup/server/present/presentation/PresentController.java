package com.zipup.server.present.presentation;

import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.dto.ParticipatePresentRequest;
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

@RestController
@RequestMapping("/api/v1/present")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Present", description = "펀딩 참여 관련 API")
public class PresentController {
  private final PresentService presentService;

  @Operation(summary = "참여할 펀딩 조회", description = "펀딩 식별자 값으로 참여할 펀딩 조회")
  @Parameter(name = "funding", description = "펀딩 식별자 값 (UUID)")
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
  public ResponseEntity<FundingDetailResponse> getFundingDetail(@RequestParam(value = "funding") String id) {
    return ResponseEntity.ok(presentService.getFundingDetail(id));
  }

  @Operation(summary = "펀딩 참여하기", description = "펀딩 식별자 값으로 참여할 펀딩 조회")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "펀딩, 참여자, 결제 내역에 대한 식별자 값과 보내는 사람 이름 & 축하 메시지")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "참여 성공",
                  content = @Content(schema = @Schema(type = "{UUID} (펀딩 참여에 대한 식별자 값)"))),
          @ApiResponse(
                  responseCode = "400",
                  description = "잘못된 UUID 형태",
                  content = @Content(schema = @Schema(type = "유효하지 않은 UUID입니다: {요청 인자}")))
  })
  @PostMapping("")
  public ResponseEntity<String> participateFunding(@RequestBody ParticipatePresentRequest request) {
    return ResponseEntity.ok(presentService.participateFunding(request));
  }
}
