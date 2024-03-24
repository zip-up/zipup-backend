package com.zipup.server.global.security.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

public class CookieUtil {
  public static final String COOKIE_TOKEN_REFRESH = "refresh-token";

  public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName()))
          return Optional.of(cookie);
      }
    }
    return Optional.empty();
  }

  public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);

    response.addCookie(cookie);
  }

  public static ResponseCookie addResponseCookie(String name, String value, int maxAge) {
    return ResponseCookie.from(name, value)
            .maxAge(maxAge)
            .sameSite("None")
            .secure(true)
            .httpOnly(true)
            .path("/")
            .build();
  }

  public static ResponseCookie addResponseCookie(HttpServletResponse response, String name, String value, int maxAge, String url) {
    String domain = extractDomain(url);

    ResponseCookie cookie = ResponseCookie.from(name, value)
            .maxAge(maxAge)
            .sameSite("None")
            .secure(true)
            .httpOnly(true)
            .path("/")
            .domain(domain)
            .build();

    response.addHeader(SET_COOKIE, cookie.toString());

    return cookie;
  }

  public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      }
    }
  }

  public static String serialize(Object obj) {
    return Base64.getUrlEncoder()
            .encodeToString(SerializationUtils.serialize(obj));
  }

  public static <T> T deserialize(Cookie cookie, Class<T> cls) throws IOException, ClassNotFoundException {
    return cls.cast(deserializeWithObjectInputStream(Base64.getUrlDecoder().decode(cookie.getValue())));
  }

  private static Object deserializeWithObjectInputStream(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
         ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
      return objectInputStream.readObject();
    }
  }

  private static String extractDomain(String url) {
    try {
      URI uri = new URI(url);
      String domain = uri.getHost();
      return domain.startsWith("www.") ? domain.substring(4) : domain;
    } catch (URISyntaxException e) {
      return "";
    }
  }

}

