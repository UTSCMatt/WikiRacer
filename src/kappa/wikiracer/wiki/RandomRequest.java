package kappa.wikiracer.wiki;

import kappa.wikiracer.exception.InvalidArticleException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class RandomRequest {

  public static String getRandom() {
    return getRandom("");
  }

  public static String getRandom(String notEqual) {
    notEqual = StringUtils.trimToEmpty(notEqual);
    String article;
    do {
      String request = "?action=query&format=json&list=random&rnnamespace=0";
      JSONObject result = SendRequest.sendRequest(request, "POST");
      article = result.getJSONObject(MediaWikiConstants.QUERY)
          .getJSONArray(MediaWikiConstants.RANDOM).getJSONObject(0)
          .getString(MediaWikiConstants.TITLE);
    } while (article.equals(notEqual));

    return article;
  }

}
