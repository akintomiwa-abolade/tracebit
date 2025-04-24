package dev.io.tracebit.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ApiKeyAuthFilter implements Filter {

    private final Set<String> validApiKeys;
    private final ConcurrentMap<String, AtomicInteger> requestCounter = new ConcurrentHashMap<>();
    private final int rateLimit;
    private final long rateLimitResetMs;

    public ApiKeyAuthFilter(
            @Value("${tracebit.api.keys:#{environment.TRACEBIT_API_KEYS}}") String apiKeys,
            @Value("${tracebit.api.rate-limit:100}") int rateLimit,
            @Value("${tracebit.api.rate-limit-reset-ms:60000}") long rateLimitResetMs) {

        // Default API key for backward compatibility
        if (apiKeys == null || apiKeys.isEmpty() || apiKeys.equals("#{environment.TRACEBIT_API_KEYS}")) {
            apiKeys = "test-api-key-123";
            log.warn("Using default API key. This is not secure for production!");
        }

        this.validApiKeys = new HashSet<>(Arrays.asList(apiKeys.split(",")));
        this.rateLimit = rateLimit;
        this.rateLimitResetMs = rateLimitResetMs;

        // Start a thread to reset rate limits periodically
        startRateLimitResetThread();
    }

    private void startRateLimitResetThread() {
        Thread resetThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(rateLimitResetMs);
                    requestCounter.clear();
                    log.debug("Rate limit counters reset");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Rate limit reset thread interrupted", e);
                    break;
                }
            }
        });
        resetThread.setDaemon(true);
        resetThread.setName("rate-limit-reset");
        resetThread.start();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String apiKey = req.getHeader("X-TRACEBIT-KEY");
        String clientIp = getClientIp(req);

        // Check if API key is valid
        if (apiKey == null || !validApiKeys.contains(apiKey)) {
            log.warn("Invalid API key attempt from IP: {}", clientIp);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Tracebit key");
            return;
        }

        // Check rate limit
        AtomicInteger counter = requestCounter.computeIfAbsent(apiKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        if (currentCount > rateLimit) {
            log.warn("Rate limit exceeded for API key: {}, IP: {}", maskApiKey(apiKey), clientIp);
            resp.setStatus(429); // 429 Too Many Requests
            resp.setHeader("X-Rate-Limit-Limit", String.valueOf(rateLimit));
            resp.setHeader("X-Rate-Limit-Remaining", "0");
            resp.setHeader("X-Rate-Limit-Reset", String.valueOf(rateLimitResetMs / 1000));
            resp.getWriter().write("Rate limit exceeded. Try again later.");
            return;
        }

        // Add rate limit headers
        resp.setHeader("X-Rate-Limit-Limit", String.valueOf(rateLimit));
        resp.setHeader("X-Rate-Limit-Remaining", String.valueOf(rateLimit - currentCount));
        resp.setHeader("X-Rate-Limit-Reset", String.valueOf(rateLimitResetMs / 1000));

        // Log successful API key usage
        log.debug("Valid API key used from IP: {}", clientIp);

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
