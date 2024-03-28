package com.zipup.server.global.security.filter;

import com.zipup.server.global.exception.CustomErrorCode;
import com.zipup.server.global.security.util.JwtProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;

  private static final String[] AUTH_LIST = {
          "/error",
          "/*/oauth2/code/*",
          "/favicon.ico",
          "/configuration/security",
          "/swagger-ui/**",
          "/webjars/**",
          "/h2-console/**",
          "/api/v1/user/sign-**",
          "/v3/api-docs/**"
  };

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    Map<String, String> cookieAttribute = request.getCookies() != null
            ? Arrays.stream(request.getCookies()).collect(Collectors.toMap(Cookie::getName, Cookie::getValue))
            : null;

    String requestURI = request.getRequestURI();
    String accessToken = jwtProvider.resolveToken(request) != null ? jwtProvider.resolveToken(request)
            : cookieAttribute == null ? null
            : cookieAttribute.get("Authorization") != null ? cookieAttribute.get("Authorization")
            : null;

    if (!StringUtils.hasText(accessToken) && Arrays.stream(AUTH_LIST).noneMatch(requestURI::contains)) {
      exceptionResponse(request, response, NOT_EXIST_TOKEN, SC_UNAUTHORIZED);
      return;
    }

    else if (StringUtils.hasText(accessToken) && Arrays.stream(AUTH_LIST).noneMatch(requestURI::contains)) {
      try{
        if (jwtProvider.validateToken(accessToken)) {
          log.error("validateToken :: {} {} {}", NOT_EXIST_TOKEN.getMessage(), StringUtils.hasText(accessToken), requestURI);
          exceptionResponse(request, response, NOT_EXIST_TOKEN, SC_UNAUTHORIZED);
          return;
        }

        Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
        request.setAttribute("exception", WRONG_TYPE_TOKEN);
      } catch (UnsupportedJwtException e){
        request.setAttribute("exception", UNSUPPORTED_TOKEN);
      } catch (JwtException e){
        request.setAttribute("exception", EXPIRED_TOKEN);
      } catch (RedisConnectionFailureException e) {
        SecurityContextHolder.clearContext();
        request.setAttribute("exception", REDIS_ERROR);
      }
    }

    filterChain.doFilter(request, response);
  }

  private void exceptionResponse(HttpServletRequest request, HttpServletResponse response, CustomErrorCode error, int status) throws IOException {
    JSONObject responseJson = new JSONObject();
    response.setStatus(status);
    response.setContentType("application/json;charset=UTF-8");

    responseJson.put("message", error.getMessage());
    responseJson.put("code", error.getCode());
    responseJson.put("path", request.getContextPath() + request.getServletPath());

    response.getWriter().print(responseJson);
  }

}
