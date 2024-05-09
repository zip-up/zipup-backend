package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.CrawlerResponse;
import com.zipup.server.funding.dto.CreateFundingRequest;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.funding.dto.SimpleFundingDataResponse;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.global.exception.ResourceNotFoundException;
import com.zipup.server.global.exception.UUIDException;
import com.zipup.server.global.security.util.AuthenticationUtil;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.global.util.entity.FundingPeriod;
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
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.exception.CustomErrorCode.INVALID_USER_UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FundServiceTest {

  @InjectMocks
  private FundService fundService;
  @Mock
  private UserService userService;
  @Mock
  private CrawlerService crawlerService;
  @Mock
  private FundRepository fundRepository;
  @Mock
  private User user;
  private Fund mockFund1;
  @Mock
  private Fund mockFund2;

  private CreateFundingRequest createFundingRequest;

  private final String userId = UUID.randomUUID().toString();
  private final String fundId = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    createFundingRequest = new CreateFundingRequest(
            null,
            "Title",
            "Road Address",
            "Detail Address",
            "Phone Number",
            "Description",
            10000,
            "https://ProductURL",
            "https://ImageURL",
            "2024-03-25T00:00:00",
            "2024-04-25T00:00:00"
    );

    mockFund1 = Fund.builder()
            .id(UUID.fromString(fundId))
            .title("mock")
            .roadAddress("mockRoad")
            .detailAddress("mockDetail")
            .phoneNumber("010-1234-1234")
            .goalPrice(10000)
            .productUrl("https://mock.com")
            .imageUrl("https://ImageURL")
            .fundingPeriod(new FundingPeriod(LocalDateTime.now(), LocalDateTime.now()))
            .user(user)
            .presents(List.of())
            .build();
  }

  @Test
  @DisplayName("funding 조회 성공")
  void testFindById_Success() {
    // given
    when(fundRepository.findById(UUID.fromString(fundId))).thenReturn(Optional.of(mockFund1));

    // when
    Fund targetFund = fundService.findById(fundId);

    // then
    assertNotNull(targetFund);
    assertEquals(targetFund, mockFund1);
    verify(fundRepository).findById(UUID.fromString(fundId));
  }

  @Test
  @DisplayName("funding 조회 없을 때")
  void testFindById_NotFound() {
    // given
    when(fundRepository.findById(UUID.fromString(fundId))).thenReturn(Optional.empty());

    // when & then
    ResourceNotFoundException thrown = assertThrows(
            ResourceNotFoundException.class,
            () -> fundService.findById(fundId)
    );
    assertNotNull(thrown);
    assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
    verify(fundRepository).findById(UUID.fromString(fundId));
  }

  @Test
  @DisplayName("funding 조회 시 invalid uuid")
  void testFindById_invalidUUID() {
    // given
    String id = "invalid-uuid";

    // when & then
    UUIDException thrown = assertThrows(
            UUIDException.class,
            () -> fundService.findById(id)
    );
    Assertions.assertNotNull(thrown);
    Assertions.assertEquals(thrown.getStatus(), INVALID_USER_UUID);
    verify(fundRepository, never()).findById(any(UUID.class));
  }

  @Test
  @DisplayName("funding 개인 조회 시 올바른 userId 있는 경우")
  void testGetMyParticipateList_ValidUser() {
    // given
    FundingSummaryResponse response = mock(FundingSummaryResponse.class);

    List<Fund> fundList = Arrays.asList(mockFund1, mockFund2);
    when(userService.findById(userId)).thenReturn(user);
    when(fundRepository.findAllByUserAndStatus(user, ColumnStatus.PUBLIC))
            .thenReturn(fundList);
    when(user.getId()).thenReturn(UUID.fromString(userId));
    when(mockFund2.toSummaryResponse()).thenReturn(response);

    // when
    List<FundingSummaryResponse> result = fundService.getMyFundingList(userId);

    // then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(response));
    verify(fundRepository).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
    verify(mockFund2).toSummaryResponse();
  }

  @Test
  @DisplayName("funding 개인 조회 시 userId 없는 경우")
  void testGetMyParticipateList_ValidUserWithNoUserId() {
    // given
    try (MockedStatic<AuthenticationUtil> mocked = mockStatic(AuthenticationUtil.class)) {
      Authentication authentication = mock(Authentication.class);
      mocked.when(AuthenticationUtil::getZipupAuthentication).thenReturn(authentication);
      when(authentication.getName()).thenReturn(userId);

      FundingSummaryResponse response = mock(FundingSummaryResponse.class);

      List<Fund> fundList = Arrays.asList(mockFund1, mockFund2);
      when(userService.findById(userId)).thenReturn(user);
      when(user.getId()).thenReturn(UUID.fromString(userId));
      when(fundRepository.findAllByUserAndStatus(user, ColumnStatus.PUBLIC))
              .thenReturn(fundList);

      when(user.getId()).thenReturn(UUID.fromString(userId));
      when(mockFund2.toSummaryResponse()).thenReturn(response);

      // when
      List<FundingSummaryResponse> result = fundService.getMyFundingList(null);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.contains(response));
      verify(fundRepository).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
      verify(mockFund2).toSummaryResponse();
    }
  }

  @Test
  @DisplayName("funding 개인 조회 시 invalid uuid")
  void testGetMyParticipateList_invalidUuid() {
    // when
    UUIDException thrown = assertThrows(
            UUIDException.class,
            () -> fundService.getMyFundingList("invalidId")
    );

    // then
    assertNotNull(thrown);
    assertEquals(thrown.getStatus(), INVALID_USER_UUID);
    verify(fundRepository, times(0)).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
  }

  @Test
  @DisplayName("funding 개인 조회 시 데이터 없는 경우")
  void testGetMyParticipateList_noData() {
    // given
    when(userService.findById(userId)).thenReturn(user);

    // when
    List<FundingSummaryResponse> result = fundService.getMyFundingList(userId);

    // then
    assertNotNull(result);
    assertEquals(0, result.size());
    verify(fundRepository).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
  }

  @Test
  @DisplayName("funding 개인 조회 시 회원 아닌 경우")
  void testGetMyParticipateList_noUser() {
    // given
    String invalidId = UUID.randomUUID().toString();
    when(userService.findById(invalidId)).thenThrow(new ResourceNotFoundException(DATA_NOT_FOUND));

    // when
    ResourceNotFoundException thrown = assertThrows(
            ResourceNotFoundException.class,
            () -> fundService.getMyFundingList(invalidId)
    );
    Assertions.assertNotNull(thrown);
    assertEquals(thrown.getStatus(), DATA_NOT_FOUND);
    verify(fundRepository, times(0)).findAllByUserAndStatus(user, ColumnStatus.PUBLIC);
  }

  @Test
  @DisplayName("funding 주최 성공")
  public void testCreatFunding_success() {
    CrawlerResponse crawlerResponse = mock(CrawlerResponse.class);

    when(crawlerResponse.getImageUrl()).thenReturn("https://ImageURL");
    when(crawlerService.crawlingProductInfo("https://ProductURL")).thenReturn(crawlerResponse);
    when(fundRepository.save(any(Fund.class))).thenReturn(mockFund1);

    SimpleFundingDataResponse response = fundService.createFunding(createFundingRequest);

    assertNotNull(response);
    assertEquals(response.getId(), mockFund1.getId().toString());
    assertEquals(response.getImageUrl(), mockFund1.getImageUrl());
    verify(fundRepository).save(isA(Fund.class));
  }

}
