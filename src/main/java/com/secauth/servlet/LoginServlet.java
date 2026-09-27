package com.secauth.servlet;

import com.secauth.dao.AccessLogDAO;
import com.secauth.dao.PolicyDAO;
import com.secauth.dao.UserDAO;
import com.secauth.model.User;
import com.secauth.util.CsrfUtil;
import com.secauth.util.IpUtil;
import com.secauth.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // timing attack 방어 — user 없어도 BCrypt 연산 강제
    private static final String DUMMY_HASH =
            "$2a$12$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    private final UserDAO userDAO = new UserDAO();
    private final AccessLogDAO accessLogDAO = new AccessLogDAO();
    private final PolicyDAO policyDAO = new PolicyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession existing = req.getSession(false);
        if (existing != null && existing.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        CsrfUtil.getOrCreate(req.getSession(true));
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // CSRF는 CsrfFilter에서 이미 검증됨
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String ip = IpUtil.getClientIp(req);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            forwardError(req, resp, "아이디와 비밀번호를 입력하세요.");
            return;
        }
        if (username.length() > 50 || password.length() > 100) {
            forwardError(req, resp, "아이디 또는 비밀번호가 잘못되었습니다.");
            return;
        }

        try {
            User user = userDAO.findByUsername(username.trim());

            // 타이밍 공격 방어: user 존재 여부와 무관하게 항상 BCrypt 연산 수행
            String hashToVerify = (user != null) ? user.getPasswordHash() : DUMMY_HASH;
            boolean passwordMatches = PasswordUtil.verify(password, hashToVerify);

            if (user == null) {
                accessLogDAO.insert(username, ip, "LOGIN", "FAILURE", "존재하지 않는 계정");
                forwardError(req, resp, "아이디 또는 비밀번호가 잘못되었습니다.");
                return;
            }

            if (!user.isEnabled()) {
                accessLogDAO.insert(username, ip, "LOGIN", "FAILURE", "비활성화된 계정");
                forwardError(req, resp, "비활성화된 계정입니다. 관리자에게 문의하세요.");
                return;
            }

            if (user.isLocked()) {
                accessLogDAO.insert(username, ip, "LOGIN", "FAILURE", "계정 잠금 중");
                forwardError(req, resp, "계정이 잠겨 있습니다. 잠시 후 다시 시도하세요.");
                return;
            }

            if (!passwordMatches) {
                int maxAttempts = policyDAO.getInt("max_failed_attempts", 5);
                int lockoutMinutes = policyDAO.getInt("lockout_minutes", 30);
                userDAO.recordFailedAttempt(user.getId(), maxAttempts, lockoutMinutes);
                accessLogDAO.insert(username, ip, "LOGIN", "FAILURE", "비밀번호 불일치");
                forwardError(req, resp, "아이디 또는 비밀번호가 잘못되었습니다.");
                return;
            }

            // session fixation 방어
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);

            userDAO.recordSuccessfulLogin(user.getId());
            accessLogDAO.insert(username, ip, "LOGIN", "SUCCESS", null);

            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            CsrfUtil.getOrCreate(session);

            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } catch (Exception e) {
            getServletContext().log("로그인 처리 오류", e);
            forwardError(req, resp, "서버 오류가 발생했습니다. 잠시 후 다시 시도하세요.");
        }
    }

    private void forwardError(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

}
