package com.cpbpc.comms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    public static Connection createConnection(Properties appProperties) throws SQLException {

        try {
            Connection conn = DriverManager.getConnection(appProperties.getProperty("db_url"),
                    appProperties.getProperty("db_username"),
                    appProperties.getProperty("db_password"));

            return conn;
        } catch (Exception e) {
            throw e;
        }
    }

}
