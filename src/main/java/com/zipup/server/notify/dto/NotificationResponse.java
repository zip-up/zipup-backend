package com.zipup.server.notify.dto;

import com.zipup.server.notify.domain.Notification;
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
    private String content;
    private String url;
    private String receiver;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.notificationType = notification.getNotificationType();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.url = notification.getReceiver();
    }
}
