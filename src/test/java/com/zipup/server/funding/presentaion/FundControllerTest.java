package com.zipup.server.funding.presentaion;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class FundControllerTest {

  @Autowired
  private MockMvc mockMvc;

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