package com.zipup.server.user.presentation;

import com.zipup.server.user.dto.UserListResponse;
import com.zipup.server.user.facade.UserFundFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {
  private MockMvc mockMvc;

  @Mock
  private UserFundFacade userFacade;
  @Mock
  private UserListResponse userListResponse;
  @Mock
  private com.zipup.server.user.domain.User targetUser;
  @InjectMocks
  private UserController userController;

  private String userId = UUID.randomUUID().toString();
  private Authentication authentication;

  @BeforeEach
  public void setup() {
    UserDetails userDetails = new User(userId, "", Collections.emptyList());
    authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  public void getUserInfo_UserExists_ReturnsUserInfo() throws Exception {
    when(userFacade.findUserById(userId)).thenReturn(targetUser);
    when(targetUser.toResponseList()).thenReturn(userListResponse);
    when(userListResponse.getId()).thenReturn(userId);

    mockMvc.perform(get("/api/v1/user")
                    .principal(authentication))
            .andExpect(status().isOk())
            .andExpect((ResultMatcher) jsonPath("$.id").value(userId));
  }

}
