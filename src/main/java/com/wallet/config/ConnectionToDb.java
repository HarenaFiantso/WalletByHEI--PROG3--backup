package com.wallet.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionToDb {
  private static final String DB_URL = System.getenv("URL");
  private static final String DB_USERNAME = System.getenv("USERNAME");
  private static final String DB_PASSWORD = System.getenv("PASSWORD");

  public static Connection getConnection() {
    Connection connection;

    try {
      connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

      if (connection != null) {
        System.out.println("Connected to Postgres server successfully");
      } else {
        System.out.println("Failed to make connection");
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to connect to database\{e.getMessage()}");
    }
    return connection;
  }
}
