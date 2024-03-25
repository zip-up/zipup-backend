package com.zipup.server.present.presentation;

import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.present.application.PresentService;
import com.zipup.server.present.dto.ParticipatePresentRequest;
import com.zipup.server.user.dto.SignInRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/present")
@RequiredArgsConstructor
@Slf4j
public class PresentController {
  private final PresentService presentService;

  @GetMapping("")
  public ResponseEntity<FundingDetailResponse> getFundingDetail(@RequestParam(value = "funding") String id) {
    return ResponseEntity.ok(presentService.getFundingDetail(id));
  }

  @PostMapping("")
  public ResponseEntity<String> participateFunding(@RequestBody ParticipatePresentRequest request) {
    return ResponseEntity.ok(presentService.participateFunding(request));
  }
}
