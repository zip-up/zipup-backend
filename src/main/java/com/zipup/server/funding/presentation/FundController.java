package com.zipup.server.funding.presentation;

import com.zipup.server.funding.application.CrawlerService;
import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fund")
@RequiredArgsConstructor
@Slf4j
public class FundController {

  private final FundService fundService;
  private final CrawlerService crawlerService;

  @GetMapping("")
  public List<CrawlerResponse> crawlingProductInfo(@RequestParam String url) {
    List<CrawlerResponse> response = crawlerService.crawlingProductInfo(url);
    return response;
  }

  @GetMapping("")
  public ResponseEntity<List<FundingSummaryResponse>> getMyFundingList(@RequestParam(value = "user-id") String id) {
    return ResponseEntity.ok(fundService.getMyFundingList(id));
  }

  @GetMapping("")
  public ResponseEntity<FundingDetailResponse> getFundingDetail(@RequestParam(value = "fund-id") String id) {
    return ResponseEntity.ok(fundService.getFundingDetail(id));
  }

  @PostMapping("")
  public ResponseEntity<CreateFundingRequest> createFunding(@RequestPart("request") CreateFundingRequest request
  ) {
    SimpleDataResponse response = fundService.createFunding(request);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

    return ResponseEntity.created(location).build();
  }

}
