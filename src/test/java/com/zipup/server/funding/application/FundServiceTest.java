package com.zipup.server.funding.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zipup.server.funding.dto.CreateFundingRequest;
import com.zipup.server.funding.dto.SimpleDataResponse;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.user.application.UserService;
import com.zipup.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
public class FundServiceTest {

  @Autowired
  private UserService userService;
  @Autowired
  private FundService fundService;
  @Autowired
  private EntityManager entityManager;
  @Autowired
  private FundRepository fundRepository;

  private CreateFundingRequest request;
  private User user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = User.builder()
            .email("email@email.com")
            .password("")
            .build();

    entityManager.persist(user);

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
            user.getId().toString()
    );

    fundService = new FundService(fundRepository, userService);
  }

  @Test
  void testCreateFunding() {
    assertNotNull(user);
    assertEquals(user.getId().toString(), request.getUser());

    SimpleDataResponse response = fundService.createFunding(request);

    assertNotNull(response.getId());
  }

}
