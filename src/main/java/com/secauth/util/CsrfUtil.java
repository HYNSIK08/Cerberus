package com.secauth.util;

import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

public class CsrfUtil {

    private static final String SESSION_ATTR = "CSRF_TOKEN";
    private static final SecureRandom RNG = new SecureRandom();

    public static String getOrCreate(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_ATTR);
        if (token == null) {
            token = generate();
            session.setAttribute(SESSION_ATTR, token);
        }
        return token;
    }

    public static boolean validate(HttpSession session, String submitted) {
        if (session == null || submitted == null) return false;
        String stored = (String) session.getAttribute(SESSION_ATTR);
        return stored != null && stored.equals(submitted);
    }

    public static void rotate(HttpSession session) {
        session.setAttribute(SESSION_ATTR, generate());
    }

    private static String generate() {
        byte[] bytes = new byte[24];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
