package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kappa.wikiracer.exception.UserNotFoundException;

public class UserDao extends Dao {

  public UserDao(String url, String username, String password) {
    super(url, username, password);
  }

  public String getUserPasswordHash(String username) throws SQLException, UserNotFoundException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT Password FROM Users WHERE Username=?";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();

    if (rs.next()) {
      String password = rs.getString("Password");
      c.close();
      stmt.close();
      rs.close();
      return password;
    } else {
      c.close();
      stmt.close();
      rs.close();
      throw new UserNotFoundException();
    }
  }

  public String createUser(String username, String hash) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Create_User(?,?)";

    stmt = c.prepareCall(sql);

    stmt.setString(1, username);
    stmt.setString(2, hash);

    ResultSet rs = stmt.executeQuery();

    rs.next();

    return rs.getString("Username");
  }

}
