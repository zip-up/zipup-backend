package com.zipup.server.notify.Repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Repository
public interface EmitterRepository {

    void save(String email, SseEmitter sseEmitter);
    void saveEventCache();
    void deleteById(String email);
    Map<String,SseEmitter> findByEmail(String email);
}
