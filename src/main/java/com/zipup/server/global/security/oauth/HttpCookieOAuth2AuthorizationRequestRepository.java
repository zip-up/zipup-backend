package com.zipup.server.global.security.oauth;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.zipup.server.global.security.util.CookieUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
  public final static String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
  public final static String REFRESH_TOKEN = HttpHeaders.AUTHORIZATION + "_REFRESH";
  public static final int COOKIE_EXPIRE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return CookieUtil.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            .map(cookie -> {
              try {
                return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
              } catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
                log.error(e.getMessage());
                log.error(e.getClass().getName());
                return null;
              }
            })
            .orElse(null);
  }

  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
    if (authorizationRequest == null) {
      CookieUtil.deleteCookie(request, response, HttpHeaders.AUTHORIZATION);
      CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
      CookieUtil.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
      CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
      return;
    }

    CookieUtil.addCookie(response, HttpHeaders.AUTHORIZATION, CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    String redirectUriAfterLogin = authorizationRequest.getRedirectUri();

    if (StringUtils.isNotBlank(redirectUriAfterLogin))
      CookieUtil.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
    return this.loadAuthorizationRequest(request);
  }

  public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
    CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    CookieUtil.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
  }

}

