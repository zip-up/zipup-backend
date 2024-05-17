package com.zipup.server.global.util;

import com.zipup.server.global.exception.UUIDException;

import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;

public class UUIDUtil {
  public static UUID isValidUUID(String id) {
    try {
      return UUID.fromString(id);
    } catch (Exception e) {
      throw new UUIDException(INVALID_USER_UUID, id);
    }
  }
}
