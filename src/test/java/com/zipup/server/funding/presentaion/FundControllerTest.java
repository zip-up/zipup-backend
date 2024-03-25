package com.zipup.server.funding.presentaion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.CreateFundingRequest;
import com.zipup.server.funding.dto.SimpleDataResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FundControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CrawlerService crawlerService;

  @MockBean
  private FundService fundServiceMock;

  private ObjectMapper objectMapper = new ObjectMapper();

  private static CreateFundingRequest request;
  private static SimpleDataResponse expectedResponse;

  @BeforeAll
  static void setUp() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();

    request = new CreateFundingRequest(
            "Title",
            "Road Address",
            "Detail Address",
            "Phone Number",
            "Description",
            10000,
            "Product URL",
            "Image URL",
            "2024-03-25T00:00:00",
            "2024-04-25T00:00:00",
            uuid1
    );

    expectedResponse = new SimpleDataResponse(uuid2);
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

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  void testCreateFunding() throws Exception {
    when(fundServiceMock.createFunding(any(CreateFundingRequest.class))).thenReturn(expectedResponse);
    String requestJson = objectMapper.writeValueAsString(request);

    MockMultipartFile jsonFile = new MockMultipartFile("request", String.valueOf(MediaType.APPLICATION_JSON), String.valueOf(MediaType.APPLICATION_JSON), requestJson.getBytes());

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/fund")
                    .file(jsonFile))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn();

    verify(fundServiceMock).createFunding(request);

    String locationHeader = result.getResponse().getHeader("Location");
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/api/v1/fund/{id}")
            .buildAndExpand(expectedResponse.getId())
            .toUri();

    assertEquals(location.toString(), locationHeader);
  }

}