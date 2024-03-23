package com.zipup.server.present.application;

import com.zipup.server.present.infrastructure.PresentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresentService {
  private final PresentRepository presentRepository;
}
