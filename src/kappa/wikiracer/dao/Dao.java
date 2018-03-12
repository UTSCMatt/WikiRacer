package kappa.wikiracer.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public abstract class Dao {
  
  private Connection connection;
  
  protected Dao(String url, String username, String password) {
    try {
      connection = DriverManager.getConnection(url, username, password);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  protected Connection getConnection() {
    return connection;
  }
  
}
