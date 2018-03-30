package kappa.wikiracer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatsDao extends Dao {

  public StatsDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  public void incrementWikiPageUse(String article) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "UPDATE Wiki_Pages SET Used = Used + 1 WHERE Title = ?";

    stmt = c.prepareStatement(sql);

    stmt.setString(1, article);
    stmt.executeUpdate();

    c.close();
    stmt.close();
  }

}
