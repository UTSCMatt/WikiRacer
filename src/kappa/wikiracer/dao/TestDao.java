package kappa.wikiracer.dao;

import java.sql.Statement;


import java.sql.Connection;
import java.sql.ResultSet;

public class TestDao extends Dao {
  
  public TestDao(String url, String username, String password) {
    super(url, username, password);
  }

  public String test() {
    
    Connection c;
    Statement stmt;
    
    String sql = "SELECT Test FROM testtable";
    
    try {
      c = getConnection();
      stmt = c.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      rs.next();
      String x = rs.getString(1);
      c.close();
      stmt.close();
      rs.close();
      return x;
    } catch (Exception e) {
      return e.getMessage();
    }
  }
}
