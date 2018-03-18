package kappa.wikiracer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Dao {

  private Connection connection;

  private String url;

  private String username;

  private String password;

  protected Dao(String url, String username, String password) throws SQLException {
    this.url = url;
    this.username = username;
    this.password = password;
    connection = DriverManager.getConnection(url, username, password);
  }

  protected Connection newConnection() throws SQLException {
    connection = DriverManager.getConnection(url, username, password);
    return connection;
  }

  protected Connection getConnection() {
    return connection;
  }

}
