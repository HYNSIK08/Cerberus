package com.secauth.servlet;

import com.secauth.dao.AccessLogDAO;
import com.secauth.dao.PolicyDAO;
import com.secauth.dao.UserDAO;
import com.secauth.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Set;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private final AccessLogDAO accessLogDAO = new AccessLogDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PolicyDAO policyDAO = new PolicyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            resp.sendRedirect(req.getContextPath() + "/admin/logs");
            return;
        }
        switch (path) {
            case "/logs"   -> showLogs(req, resp);
            case "/users"  -> showUsers(req, resp);
            case "/policy" -> showPolicy(req, resp);
            default        -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null) { resp.sendError(404); return; }

        switch (path) {
            case "/users/toggle" -> toggleUser(req, resp);
            case "/policy"       -> updatePolicy(req, resp);
            default              -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showLogs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = Math.max(parseIntParam(req.getParameter("page"), 1), 1);
        int pageSize = 20;
        int offset = (page - 1) * pageSize;
        String rawFilter = req.getParameter("result");
        String filter = "SUCCESS".equals(rawFilter) || "FAILURE".equals(rawFilter)
                ? rawFilter : null;

        try {
            long total = accessLogDAO.countTotal();
            req.setAttribute("logs", accessLogDAO.findPage(offset, pageSize, filter));
            req.setAttribute("total", total);
            req.setAttribute("totalPages", (int) Math.ceil((double) total / pageSize));
            req.setAttribute("page", page);
            req.setAttribute("filter", filter);
        } catch (Exception e) {
            getServletContext().log("로그 조회 실패", e);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/admin/logs.jsp").forward(req, resp);
    }

    private void showUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("users", userDAO.findAll());
        } catch (Exception e) {
            getServletContext().log("사용자 목록 조회 실패", e);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/admin/users.jsp").forward(req, resp);
    }

    private void showPolicy(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("policies", policyDAO.findAll());
        } catch (Exception e) {
            getServletContext().log("정책 조회 실패", e);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/admin/policy.jsp").forward(req, resp);
    }

    private void toggleUser(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("userId");
        if (idParam == null) { resp.sendError(400); return; }
        try {
            long id = Long.parseLong(idParam);

            Long sessionUserId = (Long) req.getSession(false).getAttribute("userId");
            if (sessionUserId != null && sessionUserId == id) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "자신의 계정은 비활성화할 수 없습니다.");
                return;
            }

            User u = userDAO.findById(id);
            if (u != null) userDAO.setEnabled(id, !u.isEnabled());
        } catch (NumberFormatException e) {
            resp.sendError(400);
            return;
        } catch (Exception e) {
            getServletContext().log("사용자 상태 변경 실패", e);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private static final Set<String> NUMERIC_POLICY_KEYS =
            Set.of("max_failed_attempts", "lockout_minutes", "session_timeout", "password_min_length");

    private void updatePolicy(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String[] keys = req.getParameterValues("key");
        String[] vals = req.getParameterValues("value");
        if (keys != null && vals != null) {
            for (int i = 0; i < keys.length && i < vals.length; i++) {
                String key = keys[i];
                String val = vals[i];
                if (!NUMERIC_POLICY_KEYS.contains(key)) {
                    getServletContext().log("unknown policy key: " + key);
                    continue;
                }
                try {
                    int n = Integer.parseInt(val);
                    if (n < 1 || n > 9999) {
                        getServletContext().log("정책 값 범위 오류: " + key + "=" + val);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    getServletContext().log("정책 값 형식 오류: " + key + "=" + val);
                    continue;
                }
                try {
                    policyDAO.set(key, val);
                } catch (Exception e) {
                    getServletContext().log("정책 저장 실패: " + key, e);
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/policy?saved=1");
    }

    private int parseIntParam(String s, int def) {
        try { return s != null ? Integer.parseInt(s) : def; }
        catch (NumberFormatException e) { return def; }
    }
}
