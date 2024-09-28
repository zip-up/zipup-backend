package com.zipup.server.notify.application;

import com.zipup.server.notify.Repository.EmitterRepository;
import com.zipup.server.notify.Repository.NotificationRepository;
import com.zipup.server.notify.domain.Notification;
import com.zipup.server.notify.dto.NotificationResponse;
import com.zipup.server.notify.dto.NotificationType;
import com.zipup.server.global.exception.CustomErrorCode;
import com.zipup.server.global.exception.UserException;
import com.zipup.server.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;

    private static final Long DEFAULT_TIMEOUT = 600L * 1000 * 60;
    public SseEmitter subscribe(CustomUserDetails user) {
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(user.getEmail(), sseEmitter);

        sseEmitter.onCompletion(() -> emitterRepository.deleteById(user.getEmail()));
        sseEmitter.onTimeout(() -> emitterRepository.deleteById(user.getEmail()));
        sendToClient(sseEmitter,user.getEmail(), "EventStream Created. [Eamil="+ user.getEmail() + "]");

        return sseEmitter;
    }
    private void sendToClient(SseEmitter sseEmitter, String email, Object data) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(email)
                    .data(data)
            );
        } catch (Exception e) {
            emitterRepository.deleteById(email);
            throw  new UserException(CustomErrorCode.SSE_ERROR, email);
        }
    }
    public void send(String receiver, NotificationType notificationType, String title, String content, String url) {
        Notification notification = notificationRepository.save(new Notification(notificationType, title, content, url, receiver, false));

        Map<String, SseEmitter> emitters = emitterRepository.findByEmail(receiver);
        emitters.forEach(
                (key, emitter) -> {
                    sendToClient(emitter, key, new NotificationResponse(notification));
                }
        );
    }
}

