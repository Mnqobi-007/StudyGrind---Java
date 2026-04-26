package com.studygrind.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class LoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        logger.info("→ {} {} from {}", req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
        
        chain.doFilter(request, response);
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("← {} {} - {} ({}ms)", req.getMethod(), req.getRequestURI(), res.getStatus(), duration);
    }
}