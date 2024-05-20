package com.zipup.server.user.presentation;

import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.dto.*;
import com.zipup.server.user.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

  private final UserService userService;
  @SuppressWarnings("rawtypes")
  private final UserFacade userFacade;

  @SuppressWarnings("rawtypes")
  public UserController(UserService userService, @Qualifier("userFundFacade") UserFacade userFacade) {
    this.userService = userService;
    this.userFacade = userFacade;
  }

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

  @Operation(summary = "회원 정보", description = "한 명의 사용자 정보")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "회원 정보"),
          @ApiResponse(responseCode = "404", description = "없는 회원입니다."),
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("")
  public UserListResponse getUserInfo(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user
  ) {
    return userFacade.findUserById(user.getUsername()).toResponseList();
  }

  @Operation(summary = "회원 탈퇴")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "회원 정보",
                  content = @Content(schema = @Schema(implementation = SimpleDataResponse.class))),
          @ApiResponse(responseCode = "403", description = "진행 중인 펀딩 존재",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/withdrawal")
  public SimpleDataResponse unlinkUser(
          final @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
          final @RequestBody WithdrawalRequest withdrawalRequest
  ) {
    withdrawalRequest.setUserId(user.getUsername());
    return userFacade.unlinkUser(withdrawalRequest);
  }

  @Operation(summary = "임시 데이터", description = "임시")
  @GetMapping("/temp")
  public ResponseEntity<List<UserListResponse>> getUserList() {
    List<UserListResponse> response = userService.getUserList();
    return ResponseEntity.ok(response);
  }

}
