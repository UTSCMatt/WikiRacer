package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kappa.wikiracer.exception.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;

public class UserDao extends Dao {

  public UserDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  /**
   * Get the salted hash of a user.
   *
   * @param username the user's username
   * @return password which was salted by JBCrypt
   * @throws SQLException when database has an error
   * @throws UserNotFoundException if the user is not in the database
   */
  public String getUserPasswordHash(String username) throws SQLException, UserNotFoundException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT Password FROM Users WHERE Username=?";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();

    if (rs.next()) {
      final String password = rs.getString("Password");
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

  /**
   * Create a user on the database.
   *
   * @param username the user's chosen username
   * @param hash JBCrypt salted hash
   * @return the user's username
   * @throws SQLException when database has an error
   */
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

  public Boolean userExists(String username) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT COUNT(Id) numUser FROM Users WHERE Username=?";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();
    rs.next();

    final Boolean result = rs.getInt("numUser") > 0;

    c.close();
    stmt.close();
    rs.close();

    return result;

  public void changeImage(String username, String fileName) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "UPDATE Users SET ProfilePicture=? WHERE Username=?";

    stmt = c.prepareStatement(sql);

    stmt.setString(1, fileName);
    stmt.setString(2, username);

    stmt.executeUpdate();

    c.close();
    stmt.close();
  }

  public void deleteImage(String username) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "UPDATE Users SET ProfilePicture=? WHERE Username=?";

    stmt = c.prepareStatement(sql);

    stmt.setString(1, null);
    stmt.setString(2, username);

    stmt.executeUpdate();

    c.close();
    stmt.close();
  }

  public String getImage(String username) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT ProfilePicture FROM Users WHERE Username=?";

    stmt = c.prepareStatement(sql);

    stmt.setString(1, username);

    ResultSet rs = stmt.executeQuery();

    String result = null;
    if (rs.next()) {
      result = rs.getString("ProfilePicture");
    }
    c.close();
    stmt.close();
    rs.close();
    return StringUtils.trimToEmpty(result);
  }

}
