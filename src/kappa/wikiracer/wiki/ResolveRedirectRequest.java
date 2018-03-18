package kappa.wikiracer.wiki;

import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONObject;

public class ResolveRedirectRequest {

  public static String resolveRedirect(String article) throws InvalidArticleException {
    if (SendRequest.invalidArticle(article)) {
      throw new InvalidArticleException("Articles has invalid characters");
    }
    article = SendRequest.encodeTitles(article);
    String request = "?action=query&format=json&titles=" + article + "&redirects=1";
    JSONObject result = SendRequest.sendRequest(request, "POST");
    result = result.getJSONObject(MediaWikiConstants.QUERY).getJSONObject(MediaWikiConstants.PAGES);
    String pageId = (String) result.keys().next();
    result = result.getJSONObject(pageId);
    String redirect = result.getString(MediaWikiConstants.TITLE);
    return redirect.replaceAll("&", "%26");
  }

}
