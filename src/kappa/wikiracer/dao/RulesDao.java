package kappa.wikiracer.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

public class RulesDao extends Dao {

  public RulesDao(String url, String username, String password) throws SQLException {
    super(url, username, password);
  }

  /**
   * Ban an array of categories for a given game.
   *
   * @param gameId the game's id
   * @param categories array of categories given from the front end
   * @throws SQLException when database has an error
   */
  public void banCategories(String gameId, JSONArray categories) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Ban_Category(?, ?)";

    stmt = c.prepareCall(sql);

    for (int i = 0; i < categories.length(); i++) {
      String category = categories.getString(i);
      String fixedCategory = category.replaceAll(":", "%3A");
      if (!category.startsWith("Category")) {
        fixedCategory = "Category%3A" + fixedCategory;
      }
      fixedCategory = StringUtils.trimToEmpty(fixedCategory);
      if (!fixedCategory.isEmpty()) {
        stmt.setString(1, gameId);
        stmt.setString(2, fixedCategory);
        stmt.addBatch();
      }
    }

    stmt.executeBatch();
    c.close();
    stmt.close();
  }

  /**
   * Get a set of banned categories for a game.
   *
   * @param gameId the game's id
   * @return set of categories the game has banned
   * @throws SQLException when database has an error
   */
  public Set<String> getCategories(String gameId) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT c.Category FROM Wiki_Categories c INNER JOIN banned_categories b ON b.CategoryId=c.Id WHERE GameId=(SELECT Id FROM Games WHERE GameId = ?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    ResultSet rs = stmt.executeQuery();

    Set<String> results = new HashSet<>();
    while (rs.next()) {
      String result = rs.getString("Category");
      if (!result.isEmpty()) {
        results.add(result);
      }
    }
    c.close();
    stmt.close();
    rs.close();
    return results;
  }

  /**
   * Ban an array of articles for a given game.
   *
   * @param gameId the game's id
   * @param articles array of articles given from the front end
   * @throws SQLException when database has an error
   */
  public void banArticles(String gameId, JSONArray articles) throws SQLException {
    Connection c = getConnection();
    CallableStatement stmt;

    String sql = "CALL Ban_Article(?,?)";

    stmt = c.prepareCall(sql);

    for (int i = 0; i < articles.length(); i++) {
      String article = articles.getString(i);
      String fixedArticle = StringUtils.trimToEmpty(article);
      if (!fixedArticle.isEmpty()) {
        stmt.setString(1, gameId);
        stmt.setString(2, StringUtils.trimToEmpty(article));
        stmt.addBatch();
      }
    }
    stmt.executeBatch();
    c.close();
    stmt.close();
  }

  /**
   * Get a set of banned articles for a game.
   *
   * @param gameId the game's id
   * @return set of articles the game has banned
   * @throws SQLException when database has an error
   */
  public Set<String> getArticles(String gameId) throws SQLException {
    Connection c = getConnection();
    PreparedStatement stmt;

    String sql = "SELECT p.Title FROM Wiki_Pages p INNER JOIN banned_articles b ON b.ArticleId=p.Id WHERE GameId=(SELECT Id FROM Games WHERE GameId = ?)";

    stmt = c.prepareStatement(sql);
    stmt.setString(1, gameId);
    ResultSet rs = stmt.executeQuery();

    Set<String> results = new HashSet<>();
    while (rs.next()) {
      String result = rs.getString("Title");
      if (!result.isEmpty()) {
        results.add(result);
      }
    }
    c.close();
    stmt.close();
    rs.close();
    return results;
  }

}
