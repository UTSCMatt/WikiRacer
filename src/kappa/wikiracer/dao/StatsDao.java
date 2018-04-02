package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

  /**
   * Get a list of games a user have played. Default only show finished games
   *
   * @param username find games of this user
   * @param showNonFinished shows non finished games if true
   * @param offset skip the first offset games
   * @param limit the number of results
   * @return list of games
   * @throws SQLException when database has an error
   */
  public List<String> userGames(String username, boolean showNonFinished, int offset, int limit)
      throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL User_Games(?,?,?,?)";

    stmt = c.prepareCall(sql);
    stmt.setString(1, username);
    stmt.setBoolean(2, showNonFinished);
    stmt.setInt(3, offset);
    stmt.setInt(4, limit);

    ResultSet rs = stmt.executeQuery();

    ArrayList<String> results = new ArrayList<String>();

    while (rs.next()) {
      results.add(rs.getString("GameId"));
    }
    c.close();
    stmt.close();
    rs.close();

    return results;
  }

  /**
   * Get a list of wiki pages from start to end page of a game.
   *
   * @param gameId find game of this id
   * @param username find games of this user
   * @return list of wiki page ordered from start page to end page
   * @throws SQLException when database has an error
   */
  public String userGamePath(String gameId, String username) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT w.Title FROM Wiki_Pages w INNER JOIN Paths p ON w.Id = p.PageId "
        + "INNER JOIN Users u ON u.Id=p.UserId INNER JOIN Games g ON g.Id=p.gameId "
        + "WHERE g.GameId = ? AND u.Username = ? ORDER BY p.PathOrder";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    stmt.setString(2, username);

    ResultSet rs = stmt.executeQuery();

    StringBuilder results = new StringBuilder();

    Boolean first = true;

    while (rs.next()) {
      if (!first) {
        results.append(" -> ");
      }
      first = false;
      results.append(rs.getString("Title"));
    }
    c.close();
    stmt.close();
    rs.close();

    return results.toString();
  }

  /**
   * Get a list of pages that are most used as start/end pages.
   * Will not return pages that was never set as start/end
   *
   * @param limit the number of results
   * @return list of pages
   * @throws SQLException when database has an error
   */
  public List<String> topPages(int limit) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT Title FROM Wiki_Pages WHERE Used != 0 ORDER BY Used DESC LIMIT ?";

    stmt = c.prepareStatement(sql);
    stmt.setInt(1, limit);

    ResultSet rs = stmt.executeQuery();

    ArrayList<String> results = new ArrayList<String>();

    while (rs.next()) {
      results.add(rs.getString("Title"));
    }
    c.close();
    stmt.close();
    rs.close();

    return results;
  }
}
