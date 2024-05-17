package com.zipup.server.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipup.server.global.exception.CustomErrorCode;
import com.zipup.server.global.exception.ErrorResponse;
import com.zipup.server.global.security.util.JwtProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    String accessToken = jwtProvider.resolveToken(request);
    boolean hasToken = StringUtils.hasText(accessToken);

    if (!hasToken) {
      handleTokenException(request, response, false, EMPTY_ACCESS_JWT);
      return;
    }

    else {
      try{
        if (jwtProvider.validateToken(accessToken)) {
          handleTokenException(request, response, true, EXPIRED_TOKEN);
          return;
        }
        Authentication authentication = jwtProvider.getAuthenticationByToken(accessToken);
        isValidUUID(authentication.getName());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
        handleTokenException(request, response, true, WRONG_TYPE_TOKEN);
        return;
      } catch (DecodingException | UnsupportedJwtException e) {
        handleTokenException(request, response, true, UNSUPPORTED_TOKEN);
        return;
      } catch (JwtException e) {
        handleTokenException(request, response, true, EXPIRED_TOKEN);
        return;
      } catch (RedisConnectionFailureException e) {
        handleConnectionException(request, response, true, REDIS_ERROR);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void handleTokenException(HttpServletRequest request, HttpServletResponse response, boolean hasToken, CustomErrorCode code) throws IOException {
    log.error("jwt-authentication-filter :: uri :: {}, hasToken {}, reason {}", request.getRequestURI(), hasToken, code.getMessage());

    String errorJson;
    ObjectMapper objectMapper = new ObjectMapper();
    errorJson = objectMapper.writeValueAsString(ErrorResponse.toErrorResponse(code));

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(errorJson);
  }

  private void handleConnectionException(HttpServletRequest request, HttpServletResponse response, boolean hasToken, CustomErrorCode code) throws IOException {
    log.error("jwt-authentication-filter :: uri :: {}, hasToken {}, reason {}", request.getRequestURI(), hasToken, code.getMessage());
    SecurityContextHolder.clearContext();

    String errorJson;
    ObjectMapper objectMapper = new ObjectMapper();
    errorJson = objectMapper.writeValueAsString(ErrorResponse.toErrorResponse(code));

    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(errorJson);
  }

}
