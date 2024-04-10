package com.zipup.server.funding.application;

import com.zipup.server.funding.dto.CrawlerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CrawlerServiceTest {

  @Autowired
  private CrawlerService crawlerService;

  @Test
  public void testCrawlingProductInfo() {
    String url = "https://www.apple.com/kr/shop/buy-mac/macbook-air/13%ED%98%95-%EB%AF%B8%EB%93%9C%EB%82%98%EC%9D%B4%ED%8A%B8-apple-m3-%EC%B9%A9(8%EC%BD%94%EC%96%B4-cpu-%EB%B0%8F-8%EC%BD%94%EC%96%B4-gpu)-8gb-%EB%A9%94%EB%AA%A8%EB%A6%AC-256gb";
    CrawlerResponse response = crawlerService.crawlingProductInfo(url);

    System.out.println(response.getProductName());
    System.out.println(response.getImageUrl());

    assertNotNull(response);

    assertNotNull(response.getImageUrl());
    assertNotNull(response.getProductName());
  }

}
