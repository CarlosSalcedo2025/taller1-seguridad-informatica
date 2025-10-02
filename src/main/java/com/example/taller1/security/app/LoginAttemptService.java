package com.example.taller1.security.app;

import org.springframework.stereotype.Service;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    record Attempts(int count, Instant until) {}
    private final ConcurrentHashMap<String, Attempts> map = new ConcurrentHashMap<>();
    private final int max = 5;
    private final Duration lock = Duration.ofMinutes(15);

    public boolean isBlocked(String user) {
        var a = map.getOrDefault(user, new Attempts(0, Instant.EPOCH));
        return Instant.now().isBefore(a.until());
    }
    public void onSuccess(String user) { map.remove(user); }
    public void onFailure(String user) {
        var a = map.getOrDefault(user, new Attempts(0, Instant.EPOCH));
        int next = a.count() + 1;
        var until = next >= max ? Instant.now().plus(lock) : Instant.EPOCH;
        map.put(user, new Attempts(next, until));
    }
}

