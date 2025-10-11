package com.stockleague.backend.openapi.cache;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class RealtimeKeyCache {

    private final AtomicReference<String> key = new AtomicReference<>();

    public void set(String approvalKey) {
        key.set(approvalKey);
    }

    public String get() {
        return key.get();
    }

    public boolean hasKey() {
        return key.get() != null;
    }

    public void clear() {
        key.set(null);
    }
}
