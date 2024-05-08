package com.zipup.server.user.presentation;

import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.user.application.AuthService;
import com.zipup.server.user.dto.SignInResponse;
import com.zipup.server.user.dto.TokenAndUserInfoResponse;
import com.zipup.server.user.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static com.zipup.server.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;
import static com.zipup.server.global.security.util.CookieUtil.getCookie;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "JWT 관련 API")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "최초 로그인 시 access 토큰 검증 및 회원 정보 추출")
    @Parameter(name = "redirect_url", description = "상품 URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "토큰이 성공적으로 발급되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식입니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청입니다."),
            @ApiResponse(responseCode = "403", description = "토큰이 만료되었거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류가 발생했습니다.")
    })
    @GetMapping("/authentication")
    public ResponseEntity<SignInResponse> signInWithAccessToken(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse
    ) {
        String redirectUrl = httpServletRequest.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        String accessToken = jwtProvider.resolveToken(httpServletRequest);
        if (!StringUtils.hasText(accessToken)) throw new BaseException(EMPTY_ACCESS_JWT);

        TokenAndUserInfoResponse response = authService.signInWithAccessToken(accessToken);

        httpServletResponse.addHeader(SET_COOKIE, response.getRefreshToken().toString());

        String newAccessToken = response.getAccessToken().getValue();
        response.getSignInResponse().setAccessToken(newAccessToken);

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        }
        else return ResponseEntity.ok().body(response.getSignInResponse());
    }

    @Operation(summary = "refresh token으로 신규 access token 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리프레시 토큰이 성공적으로 발급되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식입니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청입니다."),
            @ApiResponse(responseCode = "403", description = "리프레시 토큰이 만료되었거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류가 발생했습니다.")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse
    ) {
        String refreshToken = getCookie(httpServletRequest, COOKIE_TOKEN_REFRESH)
                .map(Cookie::getValue)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new BaseException(EMPTY_REFRESH_JWT));

        ResponseCookie[] newToken = authService.refresh(refreshToken);
        httpServletResponse.addHeader(SET_COOKIE, newToken[1].toString());

        return ResponseEntity.ok().body(new TokenResponse(newToken[0].getValue()));
    }

    @Operation(summary = "로그 아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공",
                    content = @Content(schema = @Schema(implementation = SimpleDataResponse.class))),
            @ApiResponse(responseCode = "400", description = "실패",
                    content = @Content(schema = @Schema(implementation = SimpleDataResponse.class))),
    })
    @PostMapping("/sign-out")
    public ResponseEntity<SimpleDataResponse> signOut(final HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request);
        if (!StringUtils.hasText(token)) throw new BaseException(EMPTY_ACCESS_JWT);

        boolean isSucceed = authService.signOut(token);
        
        return isSucceed
                ? ResponseEntity.ok().body(new SimpleDataResponse("success"))
                : ResponseEntity.badRequest().body(new SimpleDataResponse("fail"));
    }

}
