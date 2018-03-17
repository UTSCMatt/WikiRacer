package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class RulesDao extends Dao {

  public RulesDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  public void banCategories(String gameId, String[] categories) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Ban_Category(?, ?)";

    stmt = c.prepareCall(sql);

    for (String category : categories) {
      stmt.setString(1, gameId);
      String fixedCategory = category.replaceAll(":", "%3A");
      if (!category.startsWith("Category")) {
        fixedCategory = "Category%3A" + fixedCategory;
      }
      stmt.setString(2, category);
      stmt.addBatch();
    }

    stmt.executeBatch();
    c.close();
    stmt.close();
  }

  public Set<String> getCategories(String gameId) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT c.Category FROM Wiki_Categories c INNER JOIN banned_categories b ON b.CategoryId=c.Id WHERE GameId=(SELECT Id FROM Games WHERE GameId = ?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    ResultSet rs = stmt.executeQuery();

    Set<String> results = new HashSet<>();
    while (rs.next()) {
      results.add(rs.getString("Category"));
    }
    c.close();
    stmt.close();
    rs.close();
    return results;
  }

}
