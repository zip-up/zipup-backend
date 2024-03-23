package com.zipup.server.funding.application;

import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;

}
