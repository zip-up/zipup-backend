package com.zipup.server.global.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtil {

  public static Authentication getZipupAuthentication() {
    try {
      return SecurityContextHolder.getContext()
              .getAuthentication();
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

}
