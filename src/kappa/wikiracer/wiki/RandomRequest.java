package kappa.wikiracer.wiki;

import kappa.wikiracer.exception.InvalidArticleException;
import org.json.JSONObject;

public class RandomRequest {

  public static String getRandom() {
    String request = "?action=query&format=json&list=random&rnnamespace=0";
    JSONObject result = SendRequest.sendRequest(request, "POST");
    return result.getJSONObject(MediaWikiConstants.QUERY).getJSONArray(MediaWikiConstants.RANDOM).getJSONObject(0).getString(MediaWikiConstants.TITLE);
  }

}
