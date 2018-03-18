package kappa.wikiracer.wiki;

import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONObject;

public class ExistRequest {

  /**
   * Check if an article exists on Wikipedia.
   *
   * @param article the article title
   * @return true if the article exists
   * @throws InvalidArticleException when article has invalid characters
   */
  public static boolean exists(String article) throws InvalidArticleException {
    if (SendRequest.invalidArticle(article)) {
      throw new InvalidArticleException("Articles has invalid characters");
    }
    article = SendRequest.encodeTitles(article);
    String request = "?action=query&format=json&titles=" + article;
    JSONObject result = SendRequest.sendRequest(request, "POST");
    return !result.getJSONObject(MediaWikiConstants.QUERY).getJSONObject(MediaWikiConstants.PAGES).has(MediaWikiConstants.MISSING_ID);
  }
}
