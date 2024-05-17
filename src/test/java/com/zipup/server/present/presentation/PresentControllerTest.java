package com.zipup.server.present.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.dto.ParticipateCancelRequest;
import com.zipup.server.present.dto.ParticipatePresentRequest;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

  private String userId;
  private String fundId;
  private String paymentId;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final String PAYMENT_END_POINT = "/api/v1/present/";

  @BeforeEach
  void setUp() {
    User user = User.builder()
            .email("mock@mock.com")
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

    Payment payment = Payment.builder()
            .paymentKey("test_payment_key_20240505")
            .orderId("test_order_id_20240505")
            .paymentMethod("카카오페이")
            .totalAmount(1000)
            .build();
    entityManager.persist(payment);

    entityManager.flush();
    userId = user.getId().toString();
    paymentId = payment.getId().toString();
    fundId = fund.getId().toString();

    mockMvc = MockMvcBuilders.standaloneSetup(new PresentController(presentService)).build();
  }

  @Test
  @WithMockUser
  public void testParticipateFunding_success() throws Exception {
    ParticipatePresentRequest participateRequest = ParticipatePresentRequest.builder()
            .participateId(userId)
            .fundingId(fundId)
            .paymentId(paymentId)
            .build();

    String presentId = UUID.randomUUID().toString();
    SimpleDataResponse response = new SimpleDataResponse(presentId);
    given(presentService.participateFunding(any(ParticipatePresentRequest.class))).willReturn(response);

    mockMvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(participateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(presentId));
  }

  @Test
  @WithMockUser
  public void testCancelParticipateFunding_success() throws Exception {
    ParticipateCancelRequest cancelRequest = ParticipateCancelRequest.builder()
            .fundingId(fundId)
            .cancelReason("mock-reason")
            .cancelAmount(1000)
            .build();

    mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_END_POINT + "cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isOk());
  }

}