package com.cpbpc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QuickStartApp {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        String yburl = "jdbc:postgresql://3.112.18.22:5433/yugabyte";
//        DriverManager.setLoginTimeout(60);
        Connection conn = DriverManager.getConnection(yburl, "yugabyte", "yugabyte");
        Statement stmt = conn.createStatement();
        try {
            System.out.println("Connected to the PostgreSQL server successfully.");
            stmt.execute("DROP TABLE IF EXISTS employee");
            stmt.execute("CREATE TABLE IF NOT EXISTS employee" +
                    "  (id int primary key, name varchar, age int, language text)");
            System.out.println("Created table employee");

            String insertStr = "INSERT INTO employee VALUES (1, 'John', 35, 'Java')";
            stmt.execute(insertStr);
            System.out.println("EXEC: " + insertStr);

            ResultSet rs = stmt.executeQuery("select * from employee");
            while (rs.next()) {
                System.out.println(String.format("Query returned: name = %s, age = %s, language = %s",
                        rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
