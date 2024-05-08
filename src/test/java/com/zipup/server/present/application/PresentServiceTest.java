package com.zipup.server.present.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.global.exception.BaseException;
import com.zipup.server.global.exception.PaymentException;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.exception.UUIDException;
import com.zipup.server.global.security.util.AuthenticationUtil;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.payment.application.PaymentService;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.payment.dto.PaymentCancelRequest;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.ParticipateCancelRequest;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.present.infrastructure.PresentRepository;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PresentServiceTest {

  @InjectMocks
  private PresentService presentService;
  @Mock
  private PaymentService paymentService;
  @Mock
  private UserService userService;
  @Mock
  private FundService fundService;
  @Mock
  private PresentRepository presentRepository;

  @Mock
  private User user;
  @Mock
  private Fund fund;
  @Mock
  private Payment payment;
  private Present mockPresent1;
  @Mock
  private Present mockPresent2;

  private final String userId = UUID.randomUUID().toString();
  private final String fundId = UUID.randomUUID().toString();
  private final String paymentId = UUID.randomUUID().toString();
  private final String presentId = UUID.randomUUID().toString();
  private ParticipatePresentRequest participateRequest;
  private ParticipateCancelRequest cancelRequest;

  @BeforeEach
  void setUp() {
    mockPresent1 = Present.builder()
            .id(UUID.fromString(presentId))
            .payment(payment)
            .user(user)
            .fund(fund)
            .build();

    participateRequest = ParticipatePresentRequest.builder()
            .participateId(userId)
            .paymentId(paymentId)
            .fundingId(fundId)
            .congratsMessage("mock-congrats")
            .senderName("mock-sender")
            .build();

    cancelRequest = ParticipateCancelRequest.builder()
            .fundingId(fundId)
            .cancelAmount(1000)
            .cancelReason("mock-reason")
            .build();
  }

  @Test
  @DisplayName("present 조회 성공")
  void testFindPresentByUserAndFund_Success() {
    // given
    when(presentRepository.findByUserAndFund(user, fund)).thenReturn(Optional.of(mockPresent1));

    // when
    Optional<Present> targetPresent = presentRepository.findByUserAndFund(user, fund);

    // then
    Assertions.assertTrue(targetPresent.isPresent());
    assertEquals(mockPresent1, targetPresent.get());
    verify(presentRepository).findByUserAndFund(user, fund);
  }

  @Test
  @DisplayName("present 조회 시 invalid user")
  void testFindPresentByUserAndFund_invalidUser() {
    // when & then
    ResourceNotFoundException thrown = assertThrows(
            ResourceNotFoundException.class,
            () -> presentService.findByUserAndFund(User.builder().build(), fund)
    );
    Assertions.assertNotNull(thrown);
    assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
    verify(presentRepository, times(0)).findByUserAndFund(user, fund);
  }

  @Test
  @DisplayName("present 조회 없을 때")
  void testFindPresentByUserAndFund_NotFound() {
    // given
    when(presentRepository.findByUserAndFund(user, fund)).thenReturn(Optional.empty());

    // when & then
    ResourceNotFoundException thrown = assertThrows(
            ResourceNotFoundException.class,
            () -> presentService.findByUserAndFund(user, fund)
    );
    Assertions.assertNotNull(thrown);
    assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
    verify(presentRepository).findByUserAndFund(user, fund);
  }

  @Test
  @DisplayName("present 개인 조회 시 올바른 userId 있는 경우")
  void testGetMyParticipateList_ValidUser() {
    // given
    FundingSummaryResponse response1 = FundingSummaryResponse.builder().build();
    FundingSummaryResponse response2 = FundingSummaryResponse.builder().build();

    List<Present> presents = Arrays.asList(mockPresent1, mockPresent2);
    when(userService.findById(userId)).thenReturn(user);
    when(presentRepository.findAllByUserAndStatus(user, ColumnStatus.PUBLIC))
            .thenReturn(presents);
    when(mockPresent2.getFund()).thenReturn(fund);
    when(fund.toSummaryResponse()).thenReturn(response1);

    // when
    List<FundingSummaryResponse> result = presentService.getMyParticipateList(userId);

    // then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(response1));
    assertTrue(result.contains(response2));
    verify(presentRepository).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
    verify(fund, times(2)).toSummaryResponse();
  }

  @Test
  @DisplayName("present 개인 조회 시 userId 없는 경우")
  void testGetMyParticipateList_ValidUserWithNoUserId() {
    // given
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      FundingSummaryResponse response1 = FundingSummaryResponse.builder().build();
      FundingSummaryResponse response2 = FundingSummaryResponse.builder().build();

      List<Present> presents = Arrays.asList(mockPresent1, mockPresent2);
      when(userService.findById(userId)).thenReturn(user);
      when(presentRepository.findAllByUserAndStatus(user, ColumnStatus.PUBLIC))
              .thenReturn(presents);
      when(mockPresent2.getFund()).thenReturn(fund);
      when(fund.toSummaryResponse()).thenReturn(response1);

      // when
      List<FundingSummaryResponse> result = presentService.getMyParticipateList(null);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.contains(response1));
      assertTrue(result.contains(response2));
      verify(presentRepository).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
      verify(fund, times(2)).toSummaryResponse();
    }
  }

  @Test
  @DisplayName("present 개인 조회 시 invalid uuid")
  void testGetMyParticipateList_invalidUuid() {
    // when
    UUIDException thrown = assertThrows(
            UUIDException.class,
            () -> presentService.getMyParticipateList("invalidId")
    );

    // then
    assertNotNull(thrown);
    assertEquals(thrown.getStatus(), INVALID_USER_UUID);
    verify(presentRepository, times(0)).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
  }

  @Test
  @DisplayName("present 개인 조회 시 데이터 없는 경우")
  void testGetMyParticipateList_noData() {
    // given
    String invalidId = UUID.randomUUID().toString();

    // when
    List<FundingSummaryResponse> result = presentService.getMyParticipateList(invalidId);

    // then
    assertNotNull(result);
    assertEquals(0, result.size());
    verify(presentRepository, times(0)).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
  }

  @Test
  @DisplayName("present 개인 조회 시 회원 아닌 경우")
  void testGetMyParticipateList_noUser() {
    // given
    String invalidId = UUID.randomUUID().toString();
    when(userService.findById(invalidId)).thenThrow(new ResourceNotFoundException(DATA_NOT_FOUND));

    // when
    ResourceNotFoundException thrown = assertThrows(
            ResourceNotFoundException.class,
            () -> presentService.getMyParticipateList(invalidId)
    );
    Assertions.assertNotNull(thrown);
    assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
    verify(presentRepository, times(0)).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
  }

  @Test
  @DisplayName("펀딩 참여 성공")
  public void testParticipateFunding_success() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      when(fundService.findById(fundId)).thenReturn(fund);
      when(userService.findById(userId)).thenReturn(user);
      when(paymentService.findById(paymentId)).thenReturn(payment);
      when(presentRepository.save(any(Present.class))).thenReturn(mockPresent1);
      SimpleDataResponse response = presentService.participateFunding(participateRequest);

      assertNotNull(response);
      assertEquals(response.getId(), mockPresent1.getId().toString());
      verify(presentRepository).save(isA(Present.class));
    }
  }

  @Test
  @DisplayName("펀딩 참여 시도 시 Spring Context에 데이터 없을 때")
  public void testParticipateFunding_NoContext() {
    lenient().when(fundService.findById(fundId)).thenReturn(fund);
    lenient().when(userService.findById(userId)).thenReturn(user);
    lenient().when(paymentService.findById(paymentId)).thenReturn(payment);

    NullPointerException thrown = assertThrows(
            NullPointerException.class,
            () -> presentService.participateFunding(participateRequest)
    );
    assertNotNull(thrown);
    verify(fundService, times(0)).findById(fundId);
    verify(userService, times(0)).findById(userId);
  }

  @Test
  @DisplayName("펀딩 참여 시도 시 invalid uuid")
  public void testParticipateFunding_InvalidUUID() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      participateRequest = ParticipatePresentRequest.builder()
              .participateId(userId)
              .paymentId(paymentId)
              .fundingId("invalidId")
              .congratsMessage("mock-congrats")
              .senderName("mock-sender")
              .build();

      lenient().when(fundService.findById("invalidId")).thenThrow(new UUIDException(INVALID_USER_UUID, "invalidId"));

      UUIDException thrown = assertThrows(
              UUIDException.class,
              () -> presentService.participateFunding(participateRequest)
      );
      assertNotNull(thrown);
      assertEquals(thrown.getStatus(), INVALID_USER_UUID);
      verify(fundService, times(0)).findById("invalidId");
      verify(userService, times(0)).findById(userId);
    }
  }

  @Test
  @DisplayName("펀딩 참여 시도 시 funding resource not found")
  public void testParticipateFunding_ResourceNotFound() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      String invalidId = UUID.randomUUID().toString();

      participateRequest = ParticipatePresentRequest.builder()
              .participateId(userId)
              .paymentId(paymentId)
              .fundingId(invalidId)
              .congratsMessage("mock-congrats")
              .senderName("mock-sender")
              .build();

      when(fundService.findById(invalidId)).thenThrow(new ResourceNotFoundException(DATA_NOT_FOUND));

      ResourceNotFoundException thrown = assertThrows(
              ResourceNotFoundException.class,
              () -> presentService.participateFunding(participateRequest)
      );
      assertNotNull(thrown);
      assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
      verify(fundService).findById(invalidId);
      verify(userService, times(0)).findById(userId);
    }
  }

  @Test
  @DisplayName("펀딩 참여 취소 성공")
  public void testCancelParticipate_success() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      when(userService.findById(userId)).thenReturn(user);
      when(user.getId()).thenReturn(UUID.fromString(userId));
      when(fundService.findById(fundId)).thenReturn(fund);
      when(presentRepository.findByUserAndFund(user, fund)).thenReturn(Optional.of(mockPresent2));
      when(mockPresent2.getUser()).thenReturn(user);
      when(mockPresent2.getPayment()).thenReturn(payment);
      when(payment.getPaymentKey()).thenReturn("payment_key");
      when(payment.getBalanceAmount()).thenReturn(10000);

      String response = presentService.cancelParticipate(cancelRequest);

      assertNotNull(response);
      assertEquals(response, "취소 성공");
      verify(presentRepository).findByUserAndFund(user, fund);
      verify(mockPresent2).setStatus(ColumnStatus.PRIVATE);
    }
  }

  @Test
  @DisplayName("펀딩 참여 취소 실패 - 자신의 참여 펀딩이 아닐 경우")
  public void testCancelParticipate_AccessDenied() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      when(userService.findById(userId)).thenReturn(user);
      when(fundService.findById(fundId)).thenReturn(fund);
      when(presentRepository.findByUserAndFund(user, fund)).thenReturn(Optional.of(mockPresent2));
      when(mockPresent2.getUser()).thenReturn(User.builder().id(UUID.randomUUID()).build());

      BaseException thrown = assertThrows(
              BaseException.class,
              () -> presentService.cancelParticipate(cancelRequest)
      );
      assertNotNull(thrown);
      assertEquals(thrown.getStatus(), ACCESS_DENIED);
      verify(fundService).findById(fundId);
      verify(presentRepository).findByUserAndFund(user, fund);
      verify(mockPresent2, times(0)).getPayment();
    }
  }

  @Test
  @DisplayName("펀딩 참여 취소 실패 - 금액 초과")
  public void testCancelParticipate_NotCancelableAmount() {
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      when(userService.findById(userId)).thenReturn(user);
      when(user.getId()).thenReturn(UUID.fromString(userId));
      when(fundService.findById(fundId)).thenReturn(fund);
      when(presentRepository.findByUserAndFund(user, fund)).thenReturn(Optional.of(mockPresent2));
      when(mockPresent2.getUser()).thenReturn(user);
      when(mockPresent2.getPayment()).thenReturn(payment);
      when(payment.getPaymentKey()).thenReturn("payment_key");
      when(payment.getBalanceAmount()).thenReturn(1);

      PaymentException thrown = assertThrows(
              PaymentException.class,
              () -> presentService.cancelParticipate(cancelRequest)
      );
      assertNotNull(thrown);
      assertEquals(thrown.getStatus(), HttpStatus.FORBIDDEN.value());
      assertEquals(thrown.getCode(), "NOT_CANCELABLE_AMOUNT");
      assertEquals(thrown.getMessage(), "취소 할 수 없는 금액 입니다.");
      verify(fundService).findById(fundId);
      verify(presentRepository).findByUserAndFund(user, fund);
      verify(paymentService, times(0)).cancelPayment(any(PaymentCancelRequest.class));
      verify(mockPresent2, times(0)).setStatus(ColumnStatus.PRIVATE);
    }
  }

}