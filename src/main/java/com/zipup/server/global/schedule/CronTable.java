package com.zipup.server.global.schedule;


import com.zipup.server.notify.application.SendService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class CronTable {
    private final SendService sendService;
    @Scheduled(cron = "0 0 9 * * ?") // 매일 아침 9시 실행
    public void run() {
        sendService.isExpired();
    }
}
