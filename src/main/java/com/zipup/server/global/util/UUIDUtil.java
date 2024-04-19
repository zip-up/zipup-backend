package com.zipup.server.global.util;

import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;

public class UUIDUtil {
  public static void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(INVALID_USER_UUID.getMessage() + id);
    }
  }
}
