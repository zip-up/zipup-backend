package com.zipup.server.funding.presentation;

import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.CrawlerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fund")
@RequiredArgsConstructor
@Slf4j
public class FundController {

  private final FundService fundService;
  private final CrawlerService crawlerService;

  @GetMapping("")
  public List<CrawlerResponse> setPublicTrashCan(@RequestParam String url) {
    List<CrawlerResponse> response = crawlerService.crawlingProductInfo(url);
    return response;
  }

}
