package com.zipup.server.global.security.handler;

import com.zipup.server.global.util.entity.LoginProvider;
import com.zipup.server.user.dto.SignInRequest;
import com.zipup.server.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.zipup.server.global.security.oauth.info.OAuth2UserInfo;
import com.zipup.server.global.security.oauth.info.OAuth2UserInfoFactory;
import com.zipup.server.global.security.util.CookieUtil;
import com.zipup.server.user.application.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;

import static com.zipup.server.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository.*;
import static com.zipup.server.global.security.util.CookieUtil.COOKIE_TOKEN_REFRESH;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final UserService userService;
  private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
  @Value("${client.address}")
  private String client;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    String targetUrl = determineTargetUrl(request, response, authentication);

    if (response.isCommitted()) {
      super.logger.error("Response has already been committed. Unable to redirect to " + targetUrl);
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.getWriter().println("ResponseAlreadyCommittedException");
      response.getWriter().flush();
      return;
    }

    clearAuthenticationAttributes(request, response);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    System.out.println("determineTargetUrl header referer :: " + request.getHeader("Referer"));

    Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_CLIENT)
            .map(Cookie::getValue);
//    if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get()))
//      throw new IllegalArgumentException("리다이렉트 uri 에러 입니다. ::" + redirectUri);
    String targetUrl = redirectUri.orElse(client);
    System.out.println("determineTarget target Url :: " + targetUrl);

    OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
    LoginProvider providerType = LoginProvider.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());

    OAuth2User user = ((OidcUser) authentication.getPrincipal());
    OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());

    SignInRequest req = SignInRequest.builder().email(userInfo.getEmail()).build();
    HttpHeaders headers = userService.signIn(req);

    List<String> accessTokenList = headers.get(HttpHeaders.AUTHORIZATION);
    List<String> refreshTokenList = headers.get(REFRESH_TOKEN);

    response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());

    String accessToken = "";
    String refreshToken;

    if ((accessTokenList != null ? accessTokenList.size() : 0) > 0) {
      accessToken = userService.resolveToken(accessTokenList.get(0), false);
      CookieUtil.addResponseAccessCookie(response, HttpHeaders.AUTHORIZATION, accessToken, COOKIE_EXPIRE_SECONDS, client);
    }

    if ((refreshTokenList != null ? refreshTokenList.size() : 0) > 0) {
      refreshToken = userService.resolveToken(refreshTokenList.get(0), true);
      CookieUtil.addResponseSecureCookie(response, COOKIE_TOKEN_REFRESH, refreshToken, COOKIE_EXPIRE_SECONDS, client);
    }

    return UriComponentsBuilder.fromUriString(targetUrl.substring(0, targetUrl.length() - 1))
//    return UriComponentsBuilder.fromUriString(client)
            .query(accessToken)
//            .queryParam(COOKIE_TOKEN_REFRESH, refreshToken)
//            .build(false)
//            .encode(StandardCharsets.UTF_8)
            .toUriString();
  }

  protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);
    URI authorizedUri = URI.create(client);

    return authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
            && authorizedUri.getPort() == clientRedirectUri.getPort();
  }

}
