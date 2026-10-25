package com.avanish.ratelimiter.filter;

import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.avanish.ratelimiter.service.RedisRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiterService rateLimiterService;

    public RateLimitFilter(RedisRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientId = request.getRemoteAddr();
        boolean allowed;

        try {
            allowed = rateLimiterService.isAllowed(clientId);
        } catch (DataAccessException ex) {
            log.error("Redis rate limiter failed", ex);

            response.setStatus(503);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"error\":\"Rate limiter unavailable. Try again later.\"}"
            );
            return;
        }

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", "1");
            response.getWriter().write(
                    "{\"error\":\"Too many requests. Try again shortly.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}