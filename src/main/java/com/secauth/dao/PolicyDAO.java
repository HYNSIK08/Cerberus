package com.secauth.dao;

import com.secauth.util.DBUtil;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PolicyDAO {

    public Map<String, String> findAll() throws SQLException {
        Map<String, String> map = new LinkedHashMap<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT policy_key, policy_value FROM security_policy");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1), rs.getString(2));
        }
        return map;
    }

    public String get(String key, String defaultValue) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT policy_value FROM security_policy WHERE policy_key=?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : defaultValue;
            }
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void set(String key, String value) throws SQLException {
        String sql = "INSERT INTO security_policy (policy_key, policy_value) VALUES (?,?) " +
                     "ON DUPLICATE KEY UPDATE policy_value=VALUES(policy_value)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }
}
