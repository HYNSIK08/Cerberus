package com.secauth.filter;

import com.secauth.dao.PolicyDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter({"/dashboard/*", "/admin/*", "/api/*"})
public class AuthFilter implements Filter {

    private final PolicyDAO policyDAO = new PolicyDAO();

    private volatile int cachedTimeoutMinutes = 30;
    private volatile long cacheExpiryMs = 0;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;

        if (!loggedIn) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long now = System.currentTimeMillis();
        if (now > cacheExpiryMs) {
            cachedTimeoutMinutes = policyDAO.getInt("session_timeout", 30);
            cacheExpiryMs = now + 60_000L;
        }
        session.setMaxInactiveInterval(cachedTimeoutMinutes * 60);

        String path = request.getServletPath();
        if (path.startsWith("/admin")) {
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
