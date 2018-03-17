package kappa.wikiracer.wiki;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONArray;
import org.json.JSONObject;

public class CategoryRequest {
  public static Boolean inCategory(String article, Set<String> categories) throws InvalidArticleException {
    if (SendRequest.invalidArticle(article)) {
      throw new InvalidArticleException("Article has invalid characters");
    }
    StringBuilder request = new StringBuilder("?action=query&format=json&prop=categories&titles=" + article + "&clcategories=");
    Boolean first = true;
    for (String category : categories) {
      if (!first) {
        request.append("%7C");
      }
      request.append(category);
    }
    JSONObject result = SendRequest.sendRequest(request.toString(), "POST");
    result = result.getJSONObject(MediaWikiConstants.QUERY).getJSONObject(MediaWikiConstants.PAGES);
    String pageId = (String) result.keys().next();
    return result.getJSONObject(pageId).has(MediaWikiConstants.CATEGORIES);
  }
}
