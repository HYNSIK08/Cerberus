package com.secauth.filter;

import com.secauth.util.CsrfUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            HttpSession session = request.getSession(false);
            String submitted = request.getParameter("_csrf");

            if (!CsrfUtil.validate(session, submitted)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF 토큰이 유효하지 않습니다.");
                return;
            }
            // POST 처리 후 토큰 교체
            if (session != null) CsrfUtil.rotate(session);
        }

        chain.doFilter(req, res);
    }
}
