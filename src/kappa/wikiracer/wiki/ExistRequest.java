package kappa.wikiracer.wiki;

import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONObject;

public class ExistRequest {
  public static boolean exists(String article) throws InvalidArticleException {
    if (article.contains("|")) {
      throw new InvalidArticleException("Articles cannot contain '|'");
    }
    String request = "?action=query&titles=" + article;
    JSONObject result = SendRequest.sendRequest(request, "POST");
    return result.getJSONObject(MediaWikiConstants.QUERY).getJSONObject(MediaWikiConstants.PAGES).has(MediaWikiConstants.MISSING_ID);
  }
}
