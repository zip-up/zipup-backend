package com.zipup.server.funding.presentaion;

import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.presentation.FundController;
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

import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FundControllerTest {

  @Mock
  private CrawlerService crawlerService;
  @Mock
  private FundService fundService;
  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new FundController(fundService, crawlerService)).build();
  }

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  void testCrawlingProductInfo() throws Exception {
    String url = "https://www.apple.com/kr/shop/buy-mac/macbook-air/13%ED%98%95-%EB%AF%B8%EB%93%9C%EB%82%98%EC%9D%B4%ED%8A%B8-apple-m3-%EC%B9%A9(8%EC%BD%94%EC%96%B4-cpu-%EB%B0%8F-8%EC%BD%94%EC%96%B4-gpu)-8gb-%EB%A9%94%EB%AA%A8%EB%A6%AC-256gb";

    // 요청 및 응답 확인
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/fund/crawler")
                    .param("product", url)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());

    verify(crawlerService, times(1)).crawlingProductInfo(url);
  }

}