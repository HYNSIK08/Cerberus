package com.secauth.filter;

import com.secauth.util.IpUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@WebFilter("/login")
public class RateLimitFilter implements Filter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MS = 10 * 60 * 1000L;

    private static final ConcurrentHashMap<String, Deque<Long>> ipAttempts =
            new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String ip = IpUtil.getClientIp(request);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = ipAttempts.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_ATTEMPTS) {
                response.setStatus(429);
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(
                    "<h3>요청이 너무 많습니다. 잠시 후 다시 시도하세요.</h3>");
                return;
            }

            timestamps.addLast(now);
        }

        chain.doFilter(req, res);
    }
}
