package com.zipup.server.funding.presentation;

import com.zipup.server.funding.application.FundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fund")
@RequiredArgsConstructor
@Slf4j
public class FundController {

  private final FundService fundService;


}
