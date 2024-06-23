package com.zipup.server.user.application;

import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.security.util.JwtProperties;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.ColumnStatus;
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
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public User findById(String id) {
    isValidUUID(id);
    return userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException(DATA_NOT_FOUND));
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

  public String resolveToken(String tokenInHeader, boolean isRefresh) {

    if (StringUtils.hasText(tokenInHeader) && tokenInHeader.startsWith(jwtProperties.getPrefix())) {
      return tokenInHeader.substring(7);
    }
    else if (isRefresh) throw new BaseException(EMPTY_REFRESH_JWT);
    else throw new BaseException(EMPTY_ACCESS_JWT);
  }

  @Transactional
  public SimpleDataResponse setPickupInfo(String userId, PickUpInfoRequest request) {
    User user = findById(userId);
    user.setPhoneNumber(request.getPhoneNumber());
    user.setDetailAddress(request.getDetailAddress());
    user.setRoadAddress(request.getRoadAddress());

    return new SimpleDataResponse(userId);
  }

  public PickUpInfoRequest getPickupInfo(String userId) {
    User user = findById(userId);
    return new PickUpInfoRequest(user.getRoadAddress(), user.getDetailAddress(), user.getPhoneNumber());
  }

  @Transactional(readOnly = true)
  public List<UserListResponse> getUserList() {
    return userRepository.findAll()
            .stream()
            .map(User::toResponseList)
            .collect(Collectors.toList());
  }

  public void unlinkStatusUser(User user) {
    user.setStatus(ColumnStatus.UNLINK);
  }

  @Transactional
  public void setWithdrawalReason(User user, String withdrawalReason) {
    user.setWithdrawalReason(withdrawalReason);
  }

}
