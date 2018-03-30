package kappa.wikiracer.dao;

import java.sql.CallableStatement;
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

  public void addToPath(String gameId, String username, String article) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Add_Path(?,?,?)";

    stmt = c.prepareCall(sql);

    stmt.setString(1, article);
    stmt.setString(2, gameId);
    stmt.setString(3, username);
    stmt.executeUpdate();

    c.close();
    stmt.close();
  }

}
