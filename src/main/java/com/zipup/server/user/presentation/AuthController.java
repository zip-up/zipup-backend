package com.zipup.server.user.presentation;

import com.zipup.server.user.application.AuthService;
import javax.servlet.http.HttpServletResponse;
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
public class AuthController {
    private final AuthService authService;

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
