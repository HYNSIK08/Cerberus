package com.secauth.listener;

import com.secauth.dao.PolicyDAO;
import com.secauth.dao.UserDAO;
import com.secauth.model.User;
import com.secauth.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.security.SecureRandom;

@WebListener
public class DataInitializer implements ServletContextListener {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            UserDAO userDAO = new UserDAO();
            PolicyDAO policyDAO = new PolicyDAO();
            int minLen = policyDAO.getInt("password_min_length", 8);

            if (userDAO.findByUsername("admin") == null) {
                String adminPass = generatePassword(Math.max(minLen, 16));
                validatePasswordLength(adminPass, minLen);

                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(PasswordUtil.hash(adminPass));
                admin.setRole("ADMIN");
                userDAO.create(admin);

                sce.getServletContext().log("[SecureAuth] admin 초기 pw: " + adminPass + " — 즉시 변경하세요");
            }

            if (userDAO.findByUsername("testuser") == null) {
                String testPass = generatePassword(Math.max(minLen, 12));
                validatePasswordLength(testPass, minLen);

                User test = new User();
                test.setUsername("testuser");
                test.setPasswordHash(PasswordUtil.hash(testPass));
                test.setRole("USER");
                userDAO.create(test);

                sce.getServletContext().log("[SecureAuth] testuser 초기 pw: " + testPass);
            }
        } catch (Exception e) {
            sce.getServletContext().log("[SecureAuth] DataInitializer 실패: " + e.getMessage(), e);
        }
    }

    public static void validatePasswordLength(String password, int minLen) {
        if (password == null || password.length() < minLen)
            throw new IllegalArgumentException("비밀번호 " + minLen + "자 이상 필요");
    }

    private String generatePassword(int length) {
        SecureRandom sr = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(sr.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
