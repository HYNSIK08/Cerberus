package com.secauth.util;

import org.mariadb.jdbc.MariaDbPoolDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final DataSource DS;

    static {
        try {
            String url = System.getenv("SECAUTH_DB_URL");
            String user = System.getenv("SECAUTH_DB_USER");
            String pass = System.getenv("SECAUTH_DB_PASS");
            int poolSize = 10;

            if (url == null) {
                // env var 없으면 로컬 dev 폴백 (WAR 패키징 제외됨)
                try (InputStream in = DBUtil.class.getClassLoader()
                        .getResourceAsStream("db.properties")) {
                    if (in == null) {
                        throw new IOException(
                            "SECAUTH_DB_URL not set and db.properties not found");
                    }
                    Properties p = new Properties();
                    p.load(in);
                    url = p.getProperty("db.url");
                    user = p.getProperty("db.user");
                    pass = p.getProperty("db.password");
                    poolSize = Integer.parseInt(p.getProperty("db.maxPoolSize", "10"));
                }
            }

            MariaDbPoolDataSource pool = new MariaDbPoolDataSource();
            pool.setUrl(url);
            pool.setUser(user);
            pool.setPassword(pass);
            pool.setMaxPoolSize(poolSize);
            DS = pool;
        } catch (IOException | SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }
}
