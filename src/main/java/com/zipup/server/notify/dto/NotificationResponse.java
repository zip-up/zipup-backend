package com.zipup.server.notify.dto;

import com.zipup.server.notify.domain.Notification;
import com.zipup.server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType notificationType;
    private String title;
    private String url;
    private String nickname;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.notificationType = notification.getNotificationType();
        this.title = notification.getTitle();
        this.url = notification.getUrl();
        this.nickname = notification.getReceiver().getName();
    }
}
