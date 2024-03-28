package com.zipup.server.global.security.handler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zipup.server.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static com.zipup.server.global.exception.CustomErrorCode.ACCESS_DENIED;

/**
 * 유저 정보는 있으나 자원에 접근할 수 있는 권한이 없는 경우 : SC_FORBIDDEN (403) 응답
 */
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  @Override
  public void handle(HttpServletRequest request,
                     HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException, ServletException {

    log.error(String.valueOf(ErrorResponse.toErrorResponse(ACCESS_DENIED).getCode()));
    log.error(ErrorResponse.toErrorResponse(ACCESS_DENIED).getMessage());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
}