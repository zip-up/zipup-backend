package com.zipup.server.notify.dto;

import lombok.Getter;
@Getter
public enum NotificationType {
    // 주최자
    USER,
    PRESENT,
    CANCEL,
    END,
    COMPLETE,


    // 참여자
    DELETE,
    LETTER;



}
