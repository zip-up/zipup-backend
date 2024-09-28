package com.zipup.server.notify.Repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EmitterRepositoryImpl implements EmitterRepository{
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();


    @Override
    public void save(String email, SseEmitter sseEmitter) {
        emitters.put(email,sseEmitter);
    }

    @Override
    public void saveEventCache() {

    }

    @Override
    public void deleteById(String email) {
        emitters.remove(email);
    }
    @Override
    public Map<String,SseEmitter> findByEmail(String email) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().equals(email))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
