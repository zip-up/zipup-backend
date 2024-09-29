package com.zipup.server.notify.infrastructure;

import com.zipup.server.notify.domain.Notification;
import com.zipup.server.notify.dto.NotificationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverEmail(String email);
}
