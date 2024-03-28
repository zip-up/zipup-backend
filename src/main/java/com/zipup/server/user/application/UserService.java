package com.zipup.server.user.application;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.*;
import com.zipup.server.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;

  private void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new BaseException(INVALID_USER_UUID);
    }
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new BaseException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public User findById(String id) {
    isValidUUID(id);
    return userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new BaseException(DATA_NOT_FOUND));
  }

  @SneakyThrows
  @Transactional
  public SignInResponse signUp(SignUpRequest request) {
    User user = createUser(request);

    return SignInResponse.builder()
            .id(user.getId().toString())
            .email(user.getEmail())
            .name(user.getName())
            .build();
  }

  @Transactional
  public User createUser(SignUpRequest request) {
    User user = User.builder()
            .email(request.getEmail())
            .name(request.getName())
            .password(request.getPassword())
            .role(UserRole.USER)
            .build();
    return userRepository.save(user);
  }

  @Transactional
  public HttpHeaders signIn(SignInRequest request) {
    User targetUser = findByEmail(request.getEmail());
    TokenResponse token = jwtProvider.generateToken(targetUser.getId().toString(), targetUser.getRole().getKey());
    return jwtProvider.setTokenHeaders(token);
  }

  public String resolveToken(String tokenInHeader) {

    if (StringUtils.hasText(tokenInHeader) && tokenInHeader.startsWith(jwtProvider.getPrefix())) {
      return tokenInHeader.substring(7);
    }
    else throw new BaseException(TOKEN_NOT_FOUND);
  }

  @Transactional(readOnly = true)
  public List<UserListResponse> getUserList() {
    return userRepository.findAll()
            .stream()
            .map(User::toResponseList)
            .collect(Collectors.toList());
  }
}
