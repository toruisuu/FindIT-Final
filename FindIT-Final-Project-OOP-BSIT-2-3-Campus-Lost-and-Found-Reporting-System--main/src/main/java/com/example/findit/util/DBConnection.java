package com.example.findit.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect() {

        try {

            String url =
                    "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres";

            String user =
                    "postgres.nowzolfypepurbxpjbwt";

            String password =
                    "vyb5gHtawnyWTGhY";

            Connection conn =
                    DriverManager.getConnection(url, user, password);

            System.out.println("Database Connected Successfully!");
            return conn;

        } catch (Exception e) {

            System.out.println("Database Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }
}
