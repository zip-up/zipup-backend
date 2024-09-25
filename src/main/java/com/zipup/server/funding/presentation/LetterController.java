package com.zipup.server.funding.presentation;

import com.zipup.server.funding.application.LetterService;
import com.zipup.server.funding.dto.*;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_ACCESS_JWT;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@RestController
@RequestMapping("/api/v1/fund/letter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Letter", description = "감사 편지 보내기")

public class LetterController {
    private final LetterService letterService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "펀딩/편지 API - 감사 편지 보러가기")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "감사 편지 조회 성공",
                    content = @Content(schema = @Schema(type = "감사편지 조회 성공",
                            implementation = FundingSummaryResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 UUID 형태",
                    content = @Content(schema = @Schema(type = "유효하지 않은 UUID입니다: {요청 인자}")))
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("")
    public LetterData getLetter(
            final HttpServletRequest httpServletRequest,
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

        return letterService.findById(fundId);
    }

    @Operation(summary = "감사 편지 생성", description = "감사 편지 생성")
    @ApiResponse(
            responseCode = "200",
            description = "감사 편지 생성 성공",
            content = @Content(schema = @Schema(implementation = LetterData.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void saveLetter(
            final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
            @RequestBody LetterData LetterData
    ) {
        letterService.saveLetter(LetterData.getId(), LetterData.getContent());
    }
}
