package com.example.findit;

import com.example.findit.util.DBConnection;

import java.sql.Connection;

public class TestDB {

    public static void main(String[] args) {

        Connection conn = DBConnection.connect();

        if(conn != null) {
            System.out.println("CONNECTED!");
        }
    }
}