package com.secauth.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.secauth.dao.AccessLogDAO;
import com.secauth.dao.UserDAO;
import com.secauth.model.AccessLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private final AccessLogDAO accessLogDAO = new AccessLogDAO();
    private final UserDAO      userDAO      = new UserDAO();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) { resp.sendError(404); return; }

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        switch (path) {
            case "/logs"  -> writeLogs(req, resp);
            case "/stats" -> writeStats(resp);
            default       -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void writeLogs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int limit = parseIntParam(req.getParameter("limit"), 50);
        try {
            List<AccessLog> logs = accessLogDAO.findRecent(Math.min(limit, 200));
            List<Map<String, Object>> out = new ArrayList<>();
            for (AccessLog l : logs) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",        l.getId());
                m.put("username",  l.getUsername());
                m.put("ip",        l.getIpAddress());
                m.put("action",    l.getAction());
                m.put("result",    l.getResult());
                m.put("detail",    l.getDetail());
                m.put("loggedAt",  l.getLoggedAt() != null ? l.getLoggedAt().format(FMT) : null);
                out.add(m);
            }
            writeJson(resp, Map.of("logs", out));
        } catch (Exception e) {
            getServletContext().log("로그 API 오류", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, Map.of("error", "로그를 불러오는 중 오류가 발생했습니다."));
        }
    }

    private void writeStats(HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalUsers",    userDAO.countAll());
            stats.put("todaySuccess",  accessLogDAO.countToday("SUCCESS"));
            stats.put("todayFailure",  accessLogDAO.countToday("FAILURE"));
            stats.put("totalLogs",     accessLogDAO.countTotal());
            writeJson(resp, stats);
        } catch (Exception e) {
            getServletContext().log("통계 API 오류", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, Map.of("error", "통계를 불러오는 중 오류가 발생했습니다."));
        }
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        MAPPER.writeValue(resp.getWriter(), obj);
    }

    private int parseIntParam(String s, int def) {
        try { return s != null ? Integer.parseInt(s) : def; }
        catch (NumberFormatException e) { return def; }
    }
}
