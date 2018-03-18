package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kappa.wikiracer.exception.GameException;

public class GameDao extends Dao {

  public GameDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  /**
   * Add a new game to the database.
   *
   * @param start the starting article
   * @param end the ending article
   * @param gameMode the game mode
   * @return the game id
   * @throws SQLException when database has an error
   */
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

  /**
   * A given user joins a given game.
   *
   * @param gameId the game's id
   * @param username the player
   * @return the article the player begins on
   * @throws SQLException when database has an error
   * @throws GameException when user is already in the game
   */
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

  /**
   * Checks if a user is in a given game.
   *
   * @param gameId the game's id
   * @param username the player
   * @return true if the player is in the game
   * @throws SQLException when database has an error
   */
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

  /**
   * Get the current page a user is on for a given game.
   *
   * @param gameId the game's id
   * @param username the player
   * @return the current page they are on
   * @throws SQLException when database has an error
   */
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

  /**
   * Get the final page for a given game.
   *
   * @param gameId the game's id
   * @return the article name that is the end
   * @throws SQLException when database has an error
   */
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

  /**
   * Change page for a given user's game.
   *
   * @param gameId the game id being played
   * @param username the user who is playing
   * @param nextPage the page they are going to
   * @param finished whether the next page is equal to the final page
   * @return map which maps "clicks" to number of clicks and "time" to time used
   * @throws SQLException when database has an error
   */
  public Map<String, Object> changePage(String gameId, String username, String nextPage, Boolean finished) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL change_page(?,?,?,?)";

    stmt = c.prepareCall(sql);
    stmt.setString(1, gameId);
    stmt.setString(2, username);
    stmt.setString(3, nextPage);
    stmt.setBoolean(4, finished);

    ResultSet rs = stmt.executeQuery();

    rs.next();

    Map<String, Object> result = new HashMap<>();
    result.put("clicks", rs.getString("NumClicks"));
    result.put("time", rs.getInt("usedTime"));

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
