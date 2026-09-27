package com.secauth.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

public final class IpUtil {

    private static final Set<String> TRUSTED_PROXIES = Set.of("127.0.0.1", "::1");

    private IpUtil() {}

    // X-Forwarded-For는 신뢰 프록시에서만 — 직접 요청이면 remoteAddr 사용
    public static String getClientIp(HttpServletRequest req) {
        String remoteAddr = req.getRemoteAddr();
        if (TRUSTED_PROXIES.contains(remoteAddr)) {
            String xff = req.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
            String xri = req.getHeader("X-Real-IP");
            if (xri != null && !xri.isBlank()) return xri;
        }
        return remoteAddr;
    }
}
