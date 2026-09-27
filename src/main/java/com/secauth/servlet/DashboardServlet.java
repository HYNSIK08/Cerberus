package com.secauth.servlet;

import com.secauth.dao.AccessLogDAO;
import com.secauth.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final AccessLogDAO accessLogDAO = new AccessLogDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String username = (String) session.getAttribute("username");

        try {
            req.setAttribute("recentLogs", accessLogDAO.findByUsername(username, 10));
            req.setAttribute("todaySuccess", accessLogDAO.countToday("SUCCESS"));
            req.setAttribute("todayFail", accessLogDAO.countToday("FAILURE"));
            req.setAttribute("totalUsers", userDAO.countAll());
        } catch (Exception e) {
            getServletContext().log("대시보드 데이터 로딩 실패", e);
        }

        req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
    }
}
