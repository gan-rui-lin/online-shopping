package com.helloworld.onlineshopping.modules.auth.service;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoginSecurityService {

    private static final int MAX_FAILURES = 5;
    private static final int LOCK_MINUTES = 15;

    private final Map<String, LoginAttemptState> attempts = new ConcurrentHashMap<>();
    private final AtomicLong todayFailedLoginCount = new AtomicLong(0);
    private volatile LocalDate failedCounterDate = LocalDate.now();

    public void ensureLoginAllowed(String username) {
        LoginAttemptState state = attempts.get(normalize(username));
        if (state == null || state.getLockUntil() == null) {
            return;
        }
        if (state.getLockUntil().isBefore(LocalDateTime.now())) {
            attempts.remove(normalize(username));
            return;
        }
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), state.getLockUntil()) + 1;
        throw new BusinessException(429, "Too many login failures. Account is locked for " + minutes + " minute(s)");
    }

    public void onLoginSuccess(String username) {
        attempts.remove(normalize(username));
    }

    public void onLoginFailed(String username) {
        refreshDailyCounterIfNeeded();
        todayFailedLoginCount.incrementAndGet();
        String key = normalize(username);
        attempts.compute(key, (k, oldState) -> {
            LoginAttemptState state = oldState == null ? new LoginAttemptState() : oldState;
            if (state.getLockUntil() != null && state.getLockUntil().isAfter(LocalDateTime.now())) {
                return state;
            }
            state.setFailCount(state.getFailCount() + 1);
            if (state.getFailCount() >= MAX_FAILURES) {
                state.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                state.setFailCount(0);
            }
            return state;
        });
    }

    public int getMaxFailures() {
        return MAX_FAILURES;
    }

    public int getLockMinutes() {
        return LOCK_MINUTES;
    }

    public long getLockedAccountCount() {
        LocalDateTime now = LocalDateTime.now();
        return attempts.values().stream().filter(item ->
            item.getLockUntil() != null && item.getLockUntil().isAfter(now)).count();
    }

    public long getTodayFailedLoginCount() {
        refreshDailyCounterIfNeeded();
        return todayFailedLoginCount.get();
    }

    public List<String> getLockedAccounts() {
        LocalDateTime now = LocalDateTime.now();
        return attempts.entrySet().stream()
            .filter(entry -> entry.getValue().getLockUntil() != null
                && entry.getValue().getLockUntil().isAfter(now))
            .sorted(Comparator.comparing(entry -> entry.getValue().getLockUntil()))
            .map(Map.Entry::getKey)
            .toList();
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private synchronized void refreshDailyCounterIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(failedCounterDate)) {
            failedCounterDate = today;
            todayFailedLoginCount.set(0);
        }
    }

    @Data
    private static class LoginAttemptState {
        private int failCount;
        private LocalDateTime lockUntil;
    }
}
