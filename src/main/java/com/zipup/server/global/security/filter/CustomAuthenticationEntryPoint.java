package com.zipup.server.global.security.filter;

import com.zipup.server.global.exception.CustomErrorCode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 유효한 자격증명을 제공하지 않고 접근하는 경우 : SC_UNAUTHORIZED (401) 응답
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final JSONObject responseJson = new JSONObject();

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    CustomErrorCode exception = request.getAttribute("exception") == null
            ? CustomErrorCode.UNKNOWN_ERROR
            : (CustomErrorCode) request.getAttribute("exception");
    setResponse(request, response, exception, authException);
  }

  private void setResponse(HttpServletRequest request, HttpServletResponse response, CustomErrorCode errorCode, AuthenticationException authException) throws IOException {
    response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");

    responseJson.put("message", errorCode.getMessage());
    responseJson.put("code", errorCode.getCode());
    responseJson.put("path", request.getContextPath() + request.getServletPath());
    responseJson.put("auth_message", authException.getMessage());
    responseJson.put("error_name", errorCode);

    response.getWriter().print(responseJson);
  }

}
