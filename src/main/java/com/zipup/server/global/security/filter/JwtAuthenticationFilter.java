package com.zipup.server.global.security.filter;

import com.zipup.server.global.security.util.JwtProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;

  private static final String[] AUTH_LIST = { "/api/v1/user/sign-up", "/api/v1/user/sign-in" };

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

    if (!StringUtils.hasText(accessToken) && Arrays.stream(AUTH_LIST).noneMatch(requestURI::contains))
      request.setAttribute("exception", NOT_EXIST_TOKEN);

    else if (StringUtils.hasText(accessToken) && Arrays.stream(AUTH_LIST).noneMatch(requestURI::contains)) {
      try{
        if (jwtProvider.validateToken(accessToken)) {
          log.error("validateToken :: {} {} {}", NOT_EXIST_TOKEN.getMessage(), StringUtils.hasText(accessToken), requestURI);
          request.setAttribute("exception", NOT_EXIST_TOKEN);
        }

        Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (MalformedJwtException e){
        request.setAttribute("exception", WRONG_TYPE_TOKEN);
      } catch (JwtException e){
        request.setAttribute("exception", EXPIRED_TOKEN);
      } catch (RedisConnectionFailureException e) {
        SecurityContextHolder.clearContext();
        request.setAttribute("exception", REDIS_ERROR);
      }
    }

    filterChain.doFilter(request, response);
  }
}
