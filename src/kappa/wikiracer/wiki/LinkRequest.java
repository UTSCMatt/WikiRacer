package kappa.wikiracer.wiki;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Set;
import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONArray;
import org.json.JSONObject;

public class LinkRequest {

  /**
   * Get a set of articles that the given article links to.
   *
   * @param article the article to get the set of links from
   * @return set of articles which the given article links to
   * @throws InvalidArticleException when article has invalid characters
   */
  public static Set<String> sendRequest(String article) throws InvalidArticleException {
    if (SendRequest.invalidArticle(article)) {
      throw new InvalidArticleException("Articles has invalid characters");
    }
    article = SendRequest.encodeTitles(article);
    String request = "?action=query&pllimit=max&format=json&prop=links&titles=" + article + "&plnamespace=0";
    return continueRequest(request);
  }

  private static Set<String> continueRequest(String rawRequest) throws InvalidArticleException {
    Set<String> titles = new HashSet<>();
    JSONObject continueJson = null;
    do {
      String request;
      request = rawRequest;
      if (continueJson != null) {
        for (Object key : continueJson.keySet()) {
          String stringKey = (String) key;
          request += "&" + stringKey + "=" + continueJson.getString(stringKey);
        }
      }
      JSONObject json = SendRequest.sendRequest(request, "POST");
      if (json.has(MediaWikiConstants.CONTINUE)) {
        continueJson = json.getJSONObject(MediaWikiConstants.CONTINUE);
      } else {
        continueJson = null;
      }
      json = json.getJSONObject(MediaWikiConstants.QUERY).getJSONObject(MediaWikiConstants.PAGES);
      String pageId = (String) json.keys().next();
      json = json.getJSONObject(pageId);
      JSONArray links = json.getJSONArray(MediaWikiConstants.LINKS);
      for (int i = 0; i < links.length(); i++) {
        String title = links.getJSONObject(i).getString(MediaWikiConstants.TITLE);
        titles.add(links.getJSONObject(i).getString(MediaWikiConstants.TITLE));
      }
    } while (continueJson != null);
    return titles;
  }
}
