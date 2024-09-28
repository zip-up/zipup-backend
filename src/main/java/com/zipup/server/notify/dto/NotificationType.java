package com.zipup.server.notify.dto;

public enum NotificationType {
    // 주최자
    USER("님이"),
    PRESENT("참여 하였습니다"),
    CANCEL("원을 보냈어요"),
    END("펀딩 기간이 만료되었어요"),
    COMPLETE("목표 금액을 달성했어요"),


    // 참여자
    DELETE("참여한 펀딩이 삭제되었어요"),
    LETTER("감사 편지가 도착했어요");



    private final String message;
    NotificationType(String message) {
        this.message=message;
    }
}
