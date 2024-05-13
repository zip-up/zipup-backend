package com.zipup.server.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.global.security.util.JwtProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.DecodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
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

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    String errorJson;
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> cookieAttribute = request.getCookies() != null
            ? Arrays.stream(request.getCookies()).collect(Collectors.toMap(Cookie::getName, Cookie::getValue))
            : null;

    String requestURI = request.getRequestURI();
    String accessToken = jwtProvider.resolveToken(request) != null ? jwtProvider.resolveToken(request)
            : cookieAttribute == null ? null
            : cookieAttribute.get(HttpHeaders.AUTHORIZATION) != null ? cookieAttribute.get(HttpHeaders.AUTHORIZATION)
            : null;

    if (!StringUtils.hasText(accessToken)) request.setAttribute("exception", EMPTY_ACCESS_JWT);

    else if (StringUtils.hasText(accessToken)) {
      try{
        if (jwtProvider.validateToken(accessToken)) {
          log.error("validateToken :: {} {} {}", EXPIRED_TOKEN.getMessage(), StringUtils.hasText(accessToken), requestURI);
          request.setAttribute("exception", EXPIRED_TOKEN);
        }

        Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
        log.error("jwt authentication filter :: {} {} {}", WRONG_TYPE_TOKEN.getMessage(), StringUtils.hasText(accessToken), requestURI);
        request.setAttribute("exception", WRONG_TYPE_TOKEN);
        errorJson = objectMapper.writeValueAsString(ErrorResponse.toErrorResponse(WRONG_TYPE_TOKEN));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(errorJson);
        return;
      } catch (DecodingException | UnsupportedJwtException e) {
        log.error("jwt authentication filter :: {} {} {}", UNSUPPORTED_TOKEN.getMessage(), StringUtils.hasText(accessToken), requestURI);
        request.setAttribute("exception", UNSUPPORTED_TOKEN);
        errorJson = objectMapper.writeValueAsString(ErrorResponse.toErrorResponse(UNSUPPORTED_TOKEN));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(errorJson);
        return;
      } catch (JwtException e) {
        log.error("jwt authentication filter :: {} {} {}", EXPIRED_TOKEN.getMessage(), StringUtils.hasText(accessToken), requestURI);
        errorJson = objectMapper.writeValueAsString(ErrorResponse.toErrorResponse(EXPIRED_TOKEN));
        request.setAttribute("exception", EXPIRED_TOKEN);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(errorJson);
        return;
      } catch (RedisConnectionFailureException e) {
        SecurityContextHolder.clearContext();
        request.setAttribute("exception", REDIS_ERROR);
        errorJson = objectMapper.writeValueAsString(ErrorResponse.toErrorResponse(REDIS_ERROR));
        request.setAttribute("exception", REDIS_ERROR);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(errorJson);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

}
