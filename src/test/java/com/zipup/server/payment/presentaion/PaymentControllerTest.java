package com.zipup.server.payment.presentaion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.zipup.server.global.exception.CustomErrorCode.EMPTY_ACCESS_JWT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
public class PaymentControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Test
  @WithMockUser
  @DisplayName("결제 정보 저장 시 access 토큰 없어서 실패")
  public void testCheckPaymentInfo() throws Exception {
    String orderId = "1W_pCfO4rzG9szJEcThKw";
    Integer amount = 10000;
    mockMvc.perform(post("/api/v1/payment/")
                    .param("orderId", orderId)
                    .param("amount", String.valueOf(amount)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(EMPTY_ACCESS_JWT.getCode()))
            .andExpect(jsonPath("$.message").value(EMPTY_ACCESS_JWT.getMessage()))
            .andExpect(jsonPath("$.error_name").value(EMPTY_ACCESS_JWT.toString()));

    redisTemplate.delete(orderId);
  }

}
