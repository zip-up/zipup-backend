package com.zipup.server.funding.presentaion;

import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.presentation.FundController;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.global.util.entity.UserRole;
import com.zipup.server.user.domain.User;
import com.zipup.server.user.dto.TokenResponse;
import com.zipup.server.user.facade.UserFacade;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FundControllerTest {

  @Mock
  private CrawlerService crawlerService;
  @Mock
  private FundService fundService;
  @Mock
  private UserFacade userFacade;
  @Mock
  private JwtProvider jwtProvider;
  @Mock
  private TokenResponse tokenResponse;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private EntityManager entityManager;

  private User user;

  private final String FUND_END_POINT = "/api/v1/fund/";
  private String userId;
  private String fundId;

  @BeforeEach
  void setUp() {
    user = User.builder()
            .email("mock@mock.com")
            .role(UserRole.USER)
            .build();
    entityManager.persist(user);

    Fund fund = Fund.builder()
            .title("mock")
            .roadAddress("mockRoad")
            .detailAddress("mockDetail")
            .phoneNumber("010-1234-1234")
            .goalPrice(10000)
            .productUrl("https://mock.com")
            .user(user)
            .build();
    entityManager.persist(fund);

    entityManager.flush();
    userId = user.getId().toString();
    fundId = fund.getId().toString();

    mockMvc = MockMvcBuilders.standaloneSetup(new FundController(fundService, userFacade, jwtProvider, crawlerService)).build();
  }

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  void testCrawlingProductInfo() throws Exception {
    String url = "https://www.apple.com/kr/shop/buy-mac/macbook-air/13%ED%98%95-%EB%AF%B8%EB%93%9C%EB%82%98%EC%9D%B4%ED%8A%B8-apple-m3-%EC%B9%A9(8%EC%BD%94%EC%96%B4-cpu-%EB%B0%8F-8%EC%BD%94%EC%96%B4-gpu)-8gb-%EB%A9%94%EB%AA%A8%EB%A6%AC-256gb";

    mockMvc.perform(MockMvcRequestBuilders.get(FUND_END_POINT + "crawler")
                    .param("product", url)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    verify(crawlerService, times(1)).crawlingProductInfo(url);
  }

  @Test
  public void testGetFundingDetail_success() throws Exception {
    FundingDetailResponse mockResponse = FundingDetailResponse.builder()
            .id(fundId)
            .title("mock")
            .goalPrice(10000)
            .productUrl("https://mock.com")
            .build();
    given(fundService.getFundingDetail(fundId)).willReturn(mockResponse);

    mockMvc.perform(MockMvcRequestBuilders.get(FUND_END_POINT)
                    .param("funding", fundId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(fundId))
            .andExpect(jsonPath("$.title").value("mock"))
            .andExpect(jsonPath("$.goalPrice").value(10000))
            .andExpect(jsonPath("$.productUrl").value("https://mock.com"));
  }

  @Test
  public void testGetFundingDetail_noFundingId() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(FUND_END_POINT)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

}