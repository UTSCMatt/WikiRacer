package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import kappa.wikiracer.exception.GameException;

public class GameDao extends Dao {

  public GameDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  public String createGame(String start, String end, String gameMode) throws SQLException {
    getConnection().close();
    String id = generateGameId();

    Connection c = newConnection();
    CallableStatement stmt;

    String sql = "CALL Create_Game(?,?,?,?)";

    stmt = c.prepareCall(sql);
    stmt.setString(1, id);
    stmt.setString(2, start);
    stmt.setString(3, end);
    stmt.setString(4, gameMode);

    stmt.execute();

    c.close();
    stmt.close();

    return id;

  }

  public String joinGame(String gameId, String username) throws SQLException, GameException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Join_Game(?,?)";

    stmt = c.prepareCall(sql);
    stmt.setString(1, gameId);
    stmt.setString(2, username);

    ResultSet rs = stmt.executeQuery();

    rs.next();

    if (rs.getInt(1) < 0) {
      throw new GameException(rs.getString(2));
    } else {
     return rs.getString(2);
    }
  }

  public Boolean inGame(String gameId, String username) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT COUNT(GameId) FROM player_game_map WHERE GameId = (SELECT Id FROM Games WHERE GameId=?) AND UserId = (SELECT Id FROM Users WHERE Username=?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    stmt.setString(2, username);

    ResultSet rs = stmt.executeQuery();

    rs.next();

    Boolean results = rs.getInt(1) > 0;

    c.close();
    stmt.close();
    rs.close();

    return results;

  }

  public String getCurrentPage(String gameId, String username) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT w.Title FROM player_game_map m INNER JOIN Wiki_Pages w ON m.CurrentPage=w.Id WHERE m.GameId = (SELECT Id FROM Games WHERE GameId=?) AND m.UserId = (SELECT Id FROM Users WHERE Username=?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    stmt.setString(2, username);

    ResultSet rs = stmt.executeQuery();

    rs.next();

    String results = rs.getString("Title");

    c.close();
    stmt.close();
    rs.close();

    return results;

  }

  public String finalPage(String gameId) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT w.Title FROM games g INNER JOIN Wiki_Pages w ON g.EndId=w.Id WHERE g.GameId = ?";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);

    ResultSet rs = stmt.executeQuery();

    rs.next();

    String results = rs.getString("Title");

    c.close();
    stmt.close();
    rs.close();

    return results;
  }

  public int changePage(String gameId, String username, String nextPage, Boolean finished) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "UPDATE player_game_map SET NumClicks = NumClicks + 1, CurrentPage=(SELECT Id FROM wiki_pages WHERE Title=?), EndTime=CURRENT_TIMESTAMP, Finished=? WHERE GameId = (SELECT Id FROM Games WHERE GameId=?) AND UserId = (SELECT Id FROM Users WHERE Username=?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, nextPage);
    stmt.setBoolean(2, finished);
    stmt.setString(3, gameId);
    stmt.setString(4, username);

    stmt.executeUpdate();

    stmt.close();

    sql = "SELECT NumClicks FROM player_game_map WHERE GameId = (SELECT Id FROM Games WHERE GameId=?) AND UserId = (SELECT Id FROM Users WHERE Username=?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    stmt.setString(2, username);

    ResultSet rs = stmt.executeQuery();

    rs.next();
    int result = rs.getInt("NumClicks");
    c.close();
    stmt.close();
    rs.close();

    return result;

  }

  private String generateGameId() throws SQLException {
    Boolean invalid = true;

    String id;

    do {

      id = UUID.randomUUID().toString().replaceAll("-", "");
      Connection c = newConnection();
      PreparedStatement stmt;
      String sql = "SELECT COUNT(GameId) AS total FROM Games WHERE GameId=?";

      stmt = c.prepareStatement(sql);
      stmt.setString(1, id);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        if (rs.getInt("total") == 0) {
          invalid = false;
        }
      }
      c.close();
      stmt.close();
      rs.close();
    } while (invalid);
    return id;
  }

}
