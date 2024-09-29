package com.zipup.server.notify.application;

import com.zipup.server.funding.application.FundService;
import com.zipup.server.funding.dto.FundingAllResponse;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.notify.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SendService {
    private final FundService fundService;
    private final NotificationService notificationService;

    public void isExpired() {
        List<FundingAllResponse> fundList = fundService.findFundingDetailAll(ColumnStatus.PUBLIC, ColumnStatus.PUBLIC, ColumnStatus.PUBLIC);
        fundList.stream()
                .filter(response -> response.getExpirationDate() == 0)
                .forEach(response -> notificationService.send(response.getUser(), NotificationType.END,response.getTitle(), String.valueOf(response.getId())));
    }
}
