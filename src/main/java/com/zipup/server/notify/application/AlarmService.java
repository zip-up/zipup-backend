package com.zipup.server.notify.application;

import com.zipup.server.notify.dto.NotificationResponse;
import com.zipup.server.notify.infrastructure.NotificationRepository;
import com.zipup.server.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.Notification;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional (readOnly = true)
@RequiredArgsConstructor
public class AlarmService {
    private final NotificationRepository notificationRepository;


    public List<NotificationResponse> getNotificationList(CustomUserDetails user) {
        return notificationRepository.findByReceiverEmail(user.getEmail()).stream()
                .map(NotificationResponse::new)
                .collect(Collectors.toList());
    }
}
