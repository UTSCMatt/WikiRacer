package kappa.wikiracer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

abstract class Dao {

  private Connection connection;

  private String url;

  private String username;

  private String password;

  Dao(String url, String username, String password) throws SQLException {
    this.url = url;
    this.username = username;
    this.password = password;
    connection = DriverManager.getConnection(url, username, password);
  }

  Connection newConnection() throws SQLException {
    connection = DriverManager.getConnection(url, username, password);
    return connection;
  }

  Connection getConnection() {
    return connection;
  }

}
