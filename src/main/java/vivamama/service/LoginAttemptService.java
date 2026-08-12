package vivamama.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_TIME = Duration.ofMinutes(15);
    private final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) return false;
        if (Instant.now().isAfter(attempt.blockedUntil)) {
            attempts.remove(key, attempt);
            return false;
        }
        return attempt.failures >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        Attempt attempt = attempts.compute(key, (ignored, current) -> {
            Attempt next = current == null ? new Attempt() : current;
            next.failures++;
            next.blockedUntil = Instant.now().plus(BLOCK_TIME);
            if (next.failures >= MAX_ATTEMPTS) log.warn("Tentativas de login suspeitas bloqueadas");
            return next;
        });
        try { Thread.sleep(Math.min(1000L, attempt.failures * 200L)); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
    }

    public void recordSuccess(String key) { attempts.remove(key); }

    private static final class Attempt { int failures; Instant blockedUntil = Instant.EPOCH; }
}
