package com.zipup.server.user.presentation;

import com.zipup.server.user.application.UserService;
import com.zipup.server.user.dto.SignInRequest;
import com.zipup.server.user.dto.SignInResponse;
import com.zipup.server.user.dto.SignUpRequest;
import com.zipup.server.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  @PostMapping("/sign-up")
  public ResponseEntity<SignInResponse> signUp(
          @Valid @RequestPart("request") SignUpRequest request
  ) {
    SignInResponse response = userService.signUp(request);
    HttpHeaders headers = userService.signIn(SignInRequest.builder().email(response.getEmail()).build());

    return ResponseEntity.ok().headers(headers).body(response);
  }

  @PostMapping("/sign-in")
  public ResponseEntity<TokenResponse> signIn(@Valid @RequestBody SignInRequest request) {
    HttpHeaders headers = userService.signIn(request);
    return ResponseEntity.ok().headers(headers).build();
  }

}
