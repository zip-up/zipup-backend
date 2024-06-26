package com.zipup.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponse {
  private String id;
  private String name;
  private String email;
  private String profileImage;
}
