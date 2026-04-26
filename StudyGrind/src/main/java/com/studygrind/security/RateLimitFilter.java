package com.studygrind.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 30;
    private static final int WINDOW_SECONDS = 60;
    
    private final Map<String, RateLimitInfo> attempts = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        
        // Apply rate limiting only to login and registration endpoints
        if (uri.equals("/api/auth/login") || uri.equals("/api/auth/register")) {
            String clientIp = getClientIp(request);
            
            if (isBlocked(clientIp)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":\"Too many attempts. Please try again later.\"}");
                return;
            }
            
            RateLimitInfo info = attempts.computeIfAbsent(clientIp, k -> new RateLimitInfo());
            
            synchronized (info) {
                if (info.lastAttempt != null && 
                    info.lastAttempt.plusSeconds(WINDOW_SECONDS).isBefore(LocalDateTime.now())) {
                    // Reset window if time expired
                    info.attemptCount = 0;
                }
                
                info.attemptCount++;
                info.lastAttempt = LocalDateTime.now();
                
                if (info.attemptCount > MAX_ATTEMPTS) {
                    info.blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES);
                    info.attemptCount = 0;
                    
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"error\":\"Too many failed attempts. Please try again in " + BLOCK_DURATION_MINUTES + " minutes.\"}");
                    return;
                }
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isBlocked(String ip) {
        RateLimitInfo info = attempts.get(ip);
        if (info != null && info.blockedUntil != null) {
            if (LocalDateTime.now().isBefore(info.blockedUntil)) {
                return true;
            } else {
                // Block expired, remove info
                attempts.remove(ip);
            }
        }
        return false;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }
        
        String trueClientIp = request.getHeader("True-Client-IP");
        if (trueClientIp != null && !trueClientIp.isEmpty()) {
            return trueClientIp;
        }
        
        return request.getRemoteAddr();
    }
    
    public void resetAttempts(String clientIp) {
        attempts.remove(clientIp);
    }  // Reset control
    
    private static class RateLimitInfo {
        int attemptCount = 0;
        LocalDateTime lastAttempt = null;
        LocalDateTime blockedUntil = null;
    }
}