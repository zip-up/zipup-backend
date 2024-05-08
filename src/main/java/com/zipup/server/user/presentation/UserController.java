package com.zipup.server.user.presentation;

import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.dto.*;
import com.zipup.server.user.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_ACCESS_JWT;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

  private final UserService userService;
  private final UserFacade userFacade;
  private final JwtProvider jwtProvider;

  @Operation(summary = "회원 가입")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "회원가입이 성공적으로 완료되었습니다.",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = SignInResponse.class))),
          @ApiResponse(responseCode = "400", description = "요청 형식이 잘못되었습니다.")
  })
  @PostMapping("/sign-up")
  public ResponseEntity<SignInResponse> signUp(
          @Valid @RequestPart("request") SignUpRequest request
  ) {
    SignInResponse response = userService.signUp(request);
    HttpHeaders headers = userService.signIn(SignInRequest.builder().email(response.getEmail()).build());

    return ResponseEntity.ok().headers(headers).body(response);
  }

  @Operation(summary = "로그인", description = "사용자를 인증하여 토큰을 발급합니다.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "로그인이 성공적으로 완료되었습니다.",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = TokenResponse.class))),
          @ApiResponse(responseCode = "400", description = "요청 형식이 잘못되었습니다."),
          @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.")
  })
  @PostMapping("/sign-in")
  public ResponseEntity<TokenResponse> signIn(@Valid @RequestBody SignInRequest request) {
    HttpHeaders headers = userService.signIn(request);
    return ResponseEntity.ok().headers(headers).build();
  }

  @Operation(summary = "회원 탈퇴")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "회원 정보",
          content = @Content(schema = @Schema(implementation = SimpleDataResponse.class))),
          @ApiResponse(responseCode = "403", description = "진행 중인 펀딩 존재",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  @PutMapping("/withdraw")
  public ResponseEntity<SimpleDataResponse> unlinkUser(
          final HttpServletRequest request,
          final HttpServletResponse response
  ) {
    String accessToken = jwtProvider.resolveToken(request);
    if (!StringUtils.hasText(accessToken)) throw new BaseException(EMPTY_ACCESS_JWT);
    SimpleDataResponse unlinkedUserId = userFacade.unlinkUser(accessToken);

    return ResponseEntity.ok().body(unlinkedUserId);
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<UserListResponse>> getUserList() {
    List<UserListResponse> response = userService.getUserList();
    return ResponseEntity.ok(response);
  }

}
