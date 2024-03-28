package com.zipup.server.payment.presentaion;

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
  @WithMockUser(authorities = {"ROLE_USER"})
  public void testCheckPaymentInfo() throws Exception {
    String orderId = "1W_pCfO4rzG9szJEcThKw";
    Integer amount = 10000;
    mockMvc.perform(post("/api/v1/payment/")
                    .param("orderId", orderId)
                    .param("amount", String.valueOf(amount)))
            .andExpect(status().isOk())
            .andExpect(content().string("결제 진행!"));

    mockMvc.perform(post("/api/v1/payment/")
                    .param("orderId", orderId)
                    .param("amount", String.valueOf(amount)))
            .andExpect(status().isConflict())
            .andExpect(content().string("동일한 주문 번호가 존재해요."));

    redisTemplate.delete(orderId);
  }

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  public void testFailPayment() throws Exception {
    String message = "Payment failed";
    Integer code = 400;

    mockMvc.perform(get("/api/v1/payment/fail")
                    .param("message", message)
                    .param("code", String.valueOf(code)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(message));
  }

}
