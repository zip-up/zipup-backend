package com.zipup.server.global.security.oauth;

import com.zipup.server.user.domain.User;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.global.util.entity.LoginProvider;
import com.zipup.server.user.infrastructure.UserRepository;
import com.zipup.server.global.security.exception.OAuthProviderMissMatchException;
import com.zipup.server.global.security.oauth.info.OAuth2UserInfo;
import com.zipup.server.global.security.oauth.info.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private static final String ALREADY_SIGNED_UP_SOCIAL = "already_signed_up_social";
  private static final String ALREADY_SIGNED_UP_LOCAL = "already_signed_up_local";

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(userRequest);

    try {
      return this.process(userRequest, user);
    } catch (AuthenticationException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("CustomOAuth2UserService loadUser Error: {} ", ex.getMessage());
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
    }
  }

  private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
    LoginProvider providerType = LoginProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

    OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
    User targetUser = userRepository.findUserByEmail(userInfo.getEmail());

    if (targetUser != null) {
      if (targetUser.getLoginProvider() == LoginProvider.LOCAL) {
        log.error("CustomOAuth2UserService process Error: 기존 회원입니다. 자체 로그인을 이용해 주세요. ");
        throw new OAuthProviderMissMatchException(ALREADY_SIGNED_UP_LOCAL);
      }

      if (providerType != targetUser.getLoginProvider()) {
        log.error("CustomOAuth2UserService process Error: 다른 소셜에서 가입된 이메일 입니다. 해당 소셜 로그인을 이용해 주세요.");
        throw new OAuthProviderMissMatchException(ALREADY_SIGNED_UP_SOCIAL);
      }
    }
    else targetUser = createUser(userInfo, providerType);

    return UserPrincipal.create(targetUser, user.getAttributes());
  }

  private User createUser(OAuth2UserInfo userInfo, LoginProvider providerType) {
    User user = User.builder()
            .email(userInfo.getEmail())
            .name(userInfo.getName())
            .profileImage(userInfo.getImageUrl())
            .password("")
            .loginProvider(providerType)
            .role(UserRole.USER)
            .build();

    return userRepository.saveAndFlush(user);
  }

}