package com.zipup.server.present.presentation;

import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PresentControllerTest {

  @Mock
  private PresentService presentService;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private EntityManager entityManager;
  private User user;
  private String userId;

  @BeforeEach
  void setUp() {
    user = User.builder()
            .email("mock@mock.com")
            .build();
    entityManager.persist(user);
    entityManager.flush();
    userId = user.getId().toString();

    mockMvc = MockMvcBuilders.standaloneSetup(new PresentController(presentService)).build();
  }

  @Test
  public void testGetMyParticipateList() throws Exception {
    List<FundingSummaryResponse> mockResponses = new ArrayList<>();
    given(presentService.getMyParticipateList(userId)).willReturn(mockResponses);

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/present/list")
                    .param("user", userId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  public void testGetMyParticipateList_NoUserId() throws Exception {
    List<FundingSummaryResponse> mockResponses = new ArrayList<>();
    given(presentService.getMyParticipateList(null)).willReturn(mockResponses);

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/present/list")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

}