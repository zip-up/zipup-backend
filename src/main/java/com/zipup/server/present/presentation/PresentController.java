package com.zipup.server.present.presentation;

import com.zipup.server.present.application.PresentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/present")
@RequiredArgsConstructor
@Slf4j
public class PresentController {
  private final PresentService presentService;
}
