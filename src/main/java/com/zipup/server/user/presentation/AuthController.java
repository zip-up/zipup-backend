package com.zipup.server.user.presentation;

import com.zipup.server.user.application.AuthService;
import javax.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "JWT 관련 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "refresh token으로 신규 access token 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리프레시 토큰이 성공적으로 발급되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식입니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청입니다."),
            @ApiResponse(responseCode = "403", description = "리프레시 토큰이 만료되었거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류가 발생했습니다.")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(COOKIE_TOKEN_REFRESH) final String refreshToken,
            final HttpServletResponse httpServletResponse
    ) {
        ResponseCookie[] newToken = authService.refresh(refreshToken);
        httpServletResponse.addHeader(SET_COOKIE, newToken[0].toString());
        httpServletResponse.addHeader(SET_COOKIE, newToken[1].toString());

        return ResponseEntity.status(CREATED).build();
    }
}
