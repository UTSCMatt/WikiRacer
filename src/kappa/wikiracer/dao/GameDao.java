package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GameDao extends Dao {

  public GameDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
    getConnection().close();
  }

  public String createGame(String start, String end) throws SQLException {
    String id = generateGameId();

    Connection c = newConnection();
    CallableStatement stmt;

    String sql = "CALL Create_Game(?,?,?)";

    stmt = c.prepareCall(sql);
    stmt.setString(1, id);
    stmt.setString(2, start);
    stmt.setString(3, end);

    stmt.execute();

    c.close();
    stmt.close();

    return id;

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
