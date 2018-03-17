package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkDao extends Dao {

  public LinkDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  public Set<String> getLinks(String title) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT p.Title FROM Wiki_Pages p INNER JOIN Wiki_Links l ON p.Id = l.Child WHERE l.Parent = (SELECT Id FROM Wiki_Pages WHERE Title = ?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, title);
    ResultSet rs = stmt.executeQuery();

    Set<String> links = new HashSet<>();

    while (rs.next()) {
      links.add(rs.getString("Title"));
    }

    c.close();
    stmt.close();
    rs.close();

    return links;

  }

  public void addLinks(String parentTitle, Set<String> links) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Create_Link(?,?)";

    stmt = c.prepareCall(sql);

    for (String child : links) {
      stmt.setString(1, parentTitle);
      stmt.setString(2, child);
      stmt.addBatch();
    }

    stmt.executeBatch();
    c.close();
    stmt.close();

  }

  public void addPage(String title) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Add_Page(?)";

    stmt = c.prepareCall(sql);

    stmt.setString(1, title);

    stmt.execute();

    c.close();
    stmt.close();
  }

}
