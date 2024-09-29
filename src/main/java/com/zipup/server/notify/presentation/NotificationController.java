package com.zipup.server.notify.presentation;

import com.zipup.server.notify.application.AlarmService;
import com.zipup.server.notify.application.NotificationService;
import com.zipup.server.notify.dto.NotificationResponse;
import com.zipup.server.user.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final AlarmService alarmService;
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(description = "Notify Sse Subscribe ")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user) {
        return notificationService.subscribe(user);
    }
    @GetMapping("")
    public List<NotificationResponse> getNotificationList(@AuthenticationPrincipal CustomUserDetails user) {
        return alarmService.getNotificationList(user);
    }


}
