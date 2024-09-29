package com.zipup.server.notify.infrastructure;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {

    void save(String email, SseEmitter sseEmitter);
    void deleteById(String email);
    Map<String,SseEmitter> findByEmail(String email);
}
