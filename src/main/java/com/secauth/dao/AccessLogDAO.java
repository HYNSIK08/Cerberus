package com.secauth.dao;

import com.secauth.model.AccessLog;
import com.secauth.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccessLogDAO {

    public void insert(String username, String ip, String action, String result, String detail)
            throws SQLException {
        String sql = "INSERT INTO access_logs (username, ip_address, action, result, detail) VALUES (?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, ip);
            ps.setString(3, action);
            ps.setString(4, result);
            ps.setString(5, detail);
            ps.executeUpdate();
        }
    }

    public List<AccessLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT * FROM access_logs ORDER BY logged_at DESC LIMIT ?";
        return query(sql, ps -> ps.setInt(1, limit));
    }

    public List<AccessLog> findByUsername(String username, int limit) throws SQLException {
        String sql = "SELECT * FROM access_logs WHERE username=? ORDER BY logged_at DESC LIMIT ?";
        return query(sql, ps -> {
            ps.setString(1, username);
            ps.setInt(2, limit);
        });
    }

    public List<AccessLog> findPage(int offset, int pageSize, String filterResult) throws SQLException {
        String sql;
        if (filterResult != null && !filterResult.isBlank()) {
            sql = "SELECT * FROM access_logs WHERE result=? ORDER BY logged_at DESC LIMIT ? OFFSET ?";
            return query(sql, ps -> {
                ps.setString(1, filterResult);
                ps.setInt(2, pageSize);
                ps.setInt(3, offset);
            });
        }
        sql = "SELECT * FROM access_logs ORDER BY logged_at DESC LIMIT ? OFFSET ?";
        return query(sql, ps -> {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
        });
    }

    public long countToday(String result) throws SQLException {
        String sql = "SELECT COUNT(*) FROM access_logs WHERE DATE(logged_at)=CURDATE() AND result=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, result);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    public long countTotal() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM access_logs");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<AccessLog> query(String sql, Binder binder) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<AccessLog> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    private AccessLog mapRow(ResultSet rs) throws SQLException {
        AccessLog l = new AccessLog();
        l.setId(rs.getLong("id"));
        l.setUsername(rs.getString("username"));
        l.setIpAddress(rs.getString("ip_address"));
        l.setAction(rs.getString("action"));
        l.setResult(rs.getString("result"));
        l.setDetail(rs.getString("detail"));
        Timestamp ts = rs.getTimestamp("logged_at");
        if (ts != null) l.setLoggedAt(ts.toLocalDateTime());
        return l;
    }
}
