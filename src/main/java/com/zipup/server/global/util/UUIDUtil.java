package com.zipup.server.global.util;

import com.zipup.server.global.exception.UUIDException;

import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;

public class UUIDUtil {
  public static void isValidUUID(String id) {
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new UUIDException(INVALID_USER_UUID);
    }
  }
}
