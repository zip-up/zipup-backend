package com.zipup.server.payment.application;

import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.exception.UUIDException;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.*;
import com.zipup.server.payment.infrastructure.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
  @InjectMocks
  private PaymentService paymentService;
  @Mock
  private PaymentRepository paymentRepository;
  @Mock
  private RedisTemplate<String, String> redisTemplate;
  @Mock
  private ValueOperations<String, String> valueOperations;
  @Mock
  private TossService tossService;

  @Mock
  private Payment mockPayment1;
  @Mock
  private Payment mockPayment2;
  @Mock
  private Payment mockPayment3;
  private PaymentCancelRequest cancelRequest;
  private PaymentConfirmRequest confirmRequest;

  private final String paymentKey = "test_payment_key_20240505";
  private final String orderId = "test_order_id_20240505";

  @BeforeEach
  void setUp() {
    RefundReceiveAccount account = RefundReceiveAccount.builder()
            .accountNumber("accountNumber")
            .bank("88")
            .holderName("김토스")
            .build();

    confirmRequest = new PaymentConfirmRequest(orderId, 10000, paymentKey);

    cancelRequest = new PaymentCancelRequest(
            UUID.randomUUID().toString(),
            paymentKey,
            "cancelReason",
            10000,
            account
    );
  }

  @Test
  @DisplayName("payment 조회 시 invalid uuid")
  void testFindById_invalidUUID() {
    // given
    String id = "invalid-uuid";

    // when & then
    UUIDException thrown = assertThrows(
            UUIDException.class,
            () -> paymentService.findById(id)
    );
    Assertions.assertNotNull(thrown);
    Assertions.assertEquals(thrown.getStatus(), INVALID_USER_UUID);
    verify(paymentRepository, never()).findById(any(UUID.class));
  }

  @Test
  @DisplayName("payment 조회 성공")
  void testFindByIdSuccess() {
    // given
    String id = UUID.randomUUID().toString();
    when(paymentRepository.findById(UUID.fromString(id))).thenReturn(Optional.of(mockPayment1));

    // when
    Payment actualPayment = paymentService.findById(id);

    // then
    Assertions.assertNotNull(actualPayment);
    Assertions.assertEquals(mockPayment1, actualPayment);
    verify(paymentRepository).findById(UUID.fromString(id));
  }

  @Test
  @DisplayName("payment 조회 없을 때")
  void testFindByIdNotFound() {
    // given
    String id = UUID.randomUUID().toString();
    when(paymentRepository.findById(UUID.fromString(id))).thenReturn(Optional.empty());

    // when & then
    ResourceNotFoundException thrown = assertThrows(
            ResourceNotFoundException.class,
            () -> paymentService.findById(id)
    );
    Assertions.assertNotNull(thrown);
    Assertions.assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
    verify(paymentRepository).findById(UUID.fromString(id));
  }

  @Test
  @DisplayName("PaymentKey 로 Payment 객체 조회")
  void testFindByPaymentKey_success() {
    when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(mockPayment1));

    Payment targetPayment = paymentService.findByPaymentKey(paymentKey);

    verify(paymentRepository).findByPaymentKey(paymentKey);
    Assertions.assertNotNull(targetPayment);
    Assertions.assertEquals(targetPayment, mockPayment1);
  }

  @Test
  @DisplayName("PaymentKey 로 Payment 객체 조회 시 not found")
  void testFindByPaymentKey_notFound() {
    String paymentKey = "invalidKey";
    when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> paymentService.findByPaymentKey(paymentKey));
    verify(paymentRepository).findByPaymentKey(paymentKey);
  }

  @Test
  @DisplayName("orderId 존재 여부 성공")
  void testExistsByOrderId_exists() {
    when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

    Boolean exists = paymentService.existsByOrderId(orderId);

    verify(paymentRepository).existsByOrderId(orderId);
    assertTrue(exists);
  }

  @Test
  @DisplayName("orderId 존재 여부 실패")
  void testExistsByOrderId_notExists() {
    String orderId = "nonExistingOrderId";
    when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

    Boolean exists = paymentService.existsByOrderId(orderId);

    verify(paymentRepository).existsByOrderId(orderId);
    assertFalse(exists);
  }

  @Test
  @DisplayName("상점 번호가 redis 에 있는 경우")
  void testIsOrderIdExistInRedisExists() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(orderId)).thenReturn(orderId);

    Boolean exists = paymentService.isOrderIdExistInRedis(orderId, 10000, UUID.randomUUID().toString());

    assertTrue(exists);
    verify(redisTemplate.opsForValue()).get(orderId);
  }

  @Test
  @DisplayName("상점 번호가 redis 에 없는 경우")
  void testIsOrderIdExistInRedisNotExists() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(orderId)).thenReturn(null);

    Boolean exists = paymentService.isOrderIdExistInRedis(orderId, 10000, UUID.randomUUID().toString());

    assertFalse(exists);
    verify(redisTemplate.opsForValue()).get(orderId);
  }

  @Test
  @DisplayName("저장된 결제 정보 있는 경우")
  void testCheckPaymentInfoAlreadyExists() {
    // given
    Integer amount = 1000;
    String userId = UUID.randomUUID().toString();
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(orderId)).thenReturn(orderId);

    // when
    paymentService.checkPaymentInfo(orderId, amount, userId);

    // then
    verify(redisTemplate.opsForValue()).get(orderId + userId);
  }

  @Test
  @DisplayName("저장된 결제 정보 없는 경우")
  void testCheckPaymentInfoSuccess() {
    // given
    Integer amount = 1000;
    String userId = UUID.randomUUID().toString();
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(orderId)).thenReturn(null);
    doNothing().when(valueOperations).set(orderId, String.valueOf(amount));

    // when
    paymentService.checkPaymentInfo(orderId, amount, userId);

    // then
    verify(redisTemplate.opsForValue()).get(orderId + userId);
    verify(redisTemplate.opsForValue()).set(orderId + userId, String.valueOf(amount));
  }

  @Test
  @DisplayName("TossService 호출하는 지 확인")
  void checkTossService_success() {
    String paymentKey = mockPayment1.getPaymentKey();
    TossPaymentResponse expectedResponse = TossPaymentResponse.builder().paymentKey(paymentKey).build();

    when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(mockPayment1));
    when(tossService.get("/" + paymentKey, TossPaymentResponse.class)).thenReturn(Mono.just(expectedResponse));

    Mono<TossPaymentResponse> actualResponse = paymentService.fetchPaymentByPaymentKey(paymentKey);

    verify(tossService).get("/" + paymentKey, TossPaymentResponse.class);
    Assertions.assertEquals(expectedResponse, actualResponse);
  }

  @Test
  @DisplayName("payment findAll 테스트")
  void testGetPaymentAllList_success() {
    PaymentResultResponse response1 = PaymentResultResponse.builder().build();
    PaymentResultResponse response2 = PaymentResultResponse.builder().build();
    PaymentResultResponse response3 = PaymentResultResponse.builder().build();

    when(mockPayment1.toDetailResponse()).thenReturn(response1);
    when(mockPayment2.toDetailResponse()).thenReturn(response2);
    when(mockPayment3.toDetailResponse()).thenReturn(response3);

    List<Payment> mockPayments = Arrays.asList(mockPayment1, mockPayment2, mockPayment3);
    when(paymentRepository.findAll()).thenReturn(mockPayments);

    List<PaymentResultResponse> result = paymentService.getPaymentList();

    verify(paymentRepository).findAll();
    Assertions.assertNotNull(result);
    Assertions.assertEquals(3, result.size());
  }

}
