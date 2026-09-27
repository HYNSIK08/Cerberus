package com.secauth.servlet;

import com.secauth.dao.AccessLogDAO;
import com.secauth.util.IpUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final AccessLogDAO accessLogDAO = new AccessLogDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            String ip = IpUtil.getClientIp(req);
            try {
                accessLogDAO.insert(username, ip, "LOGOUT", "SUCCESS", null);
            } catch (Exception e) {
                getServletContext().log("로그아웃 로그 기록 실패", e);
            }
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // GET 로그아웃은 CSRF 우회 가능성 때문에 허용하지 않음
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
