package com.zipup.server.notify.domain;

import com.zipup.server.notify.dto.NotificationType;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private NotificationType notificationType;
    @Column(columnDefinition = "text")
    private String title;
    @Column(columnDefinition = "text")
    private String content;
    @Column(columnDefinition = "text")
    private String url;
    @Column
    private String receiver;
    @Column
    private Boolean isRead;


    public Notification(NotificationType notificationType, String title, String content, String url, String receiver, Boolean isRead) {
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.url = url;
        this.receiver = receiver;
        this.isRead = isRead;
    }

}
