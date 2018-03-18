package kappa.wikiracer.wiki;

import java.util.Set;
import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONObject;

public class CategoryRequest {

  /**
   * Checks if an article belongs to any category in a set.
   *
   * @param article the article to check with
   * @param categories the set of categories
   * @return true if the article belongs at least one of the categories
   * @throws InvalidArticleException when article has invalid characters
   */
  public static Boolean inCategory(String article, Set<String> categories)
      throws InvalidArticleException {
    if (categories.isEmpty()) {
      return false;
    }
    if (SendRequest.invalidArticle(article)) {
      throw new InvalidArticleException("Article has invalid characters");
    }
    article = SendRequest.encodeTitles(article);
    StringBuilder request = new StringBuilder(
        "?action=query&format=json&prop=categories&titles=" + article + "&clcategories=");
    Boolean first = true;
    for (String category : categories) {
      if (!first) {
        request.append("%7C");
      }
      category = SendRequest.encodeTitles(category);
      request.append(category);
    }
    JSONObject result = SendRequest.sendRequest(request.toString(), "POST");
    result = result.getJSONObject(MediaWikiConstants.QUERY).getJSONObject(MediaWikiConstants.PAGES);
    String pageId = (String) result.keys().next();
    return result.getJSONObject(pageId).has(MediaWikiConstants.CATEGORIES);
  }
}
