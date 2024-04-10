package com.zipup.server.user.presentation;

import com.zipup.server.user.application.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zipup.server.user.dto.SignInResponse;
import com.zipup.server.user.dto.TokenAndUserInfoResponse;
import com.zipup.server.user.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "JWT 관련 API")
public class AuthController {

    private final AuthService authService;

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
        String redirectUrl = httpServletRequest.getParameter("redirect_url");
        TokenAndUserInfoResponse response = authService.signInWithAccessToken(httpServletRequest);

        httpServletResponse.addHeader(SET_COOKIE, response.getAccessToken().toString());
        httpServletResponse.addHeader(SET_COOKIE, response.getRefreshToken().toString());

        String newAccessToken = response.getAccessToken().getValue();
        String newRefreshToken = response.getRefreshToken().getValue();
        response.getSignInResponse().setAccessToken(newAccessToken);
        response.getSignInResponse().setRefreshToken(newRefreshToken);

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } else {
            return ResponseEntity.ok().body(response.getSignInResponse());
        }
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
            @CookieValue(COOKIE_TOKEN_REFRESH) final String refreshToken,
            final HttpServletResponse httpServletResponse
    ) {
        ResponseCookie[] newToken = authService.refresh(refreshToken);
        httpServletResponse.addHeader(SET_COOKIE, newToken[0].toString());
        httpServletResponse.addHeader(SET_COOKIE, newToken[1].toString());

        return ResponseEntity.ok().body(new TokenResponse(newToken[0].toString(), newToken[1].toString()));
    }

    @Operation(summary = "로그 아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "로그 아웃 성공"),
            @ApiResponse(responseCode = "400", description = "로그 아웃 실패"),
    })
    @PostMapping("/sign-out")
    public ResponseEntity<String> signOut(final HttpServletRequest request) {
        return authService.signOut(request)
                ? ResponseEntity.ok().body("로그 아웃 완료")
                : ResponseEntity.ok().body("로그 아웃 실패");
    }

}
