package com.avanish.ratelimiter.filter;

import com.avanish.ratelimiter.service.RedisRateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RedisRateLimiterService service;
    private RateLimitFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        service = mock(RedisRateLimiterService.class);
        filter = new RateLimitFilter(service);

        request = new MockHttpServletRequest();
        request.setServletPath("/api/hello");
        request.setRemoteAddr("127.0.0.1");

        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @Test
    void allowedRequestContinuesToController() throws Exception {
        when(service.isAllowed("127.0.0.1")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertSame(request, chain.getRequest());
        verify(service, times(1)).isAllowed("127.0.0.1");
    }

    @Test
    void exhaustedBucketReturns429AndStopsRequest() throws Exception {
        when(service.isAllowed("127.0.0.1")).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
        assertEquals("1", response.getHeader("Retry-After"));
        assertNull(chain.getRequest());
        verify(service, times(1)).isAllowed("127.0.0.1");
    }

    @Test
    void redisFailureReturns503AndStopsRequest() throws Exception {
        when(service.isAllowed("127.0.0.1"))
                .thenThrow(new RedisConnectionFailureException(
                        "Simulated Redis outage"
                ));

        filter.doFilter(request, response, chain);

        assertEquals(503, response.getStatus());
        assertEquals(
                "{\"error\":\"Rate limiter unavailable. Try again later.\"}",
                response.getContentAsString()
        );
        assertNull(chain.getRequest());
        verify(service, times(1)).isAllowed("127.0.0.1");
    }
}