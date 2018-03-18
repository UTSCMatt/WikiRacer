package kappa.wikiracer.wiki;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

public class SendRequest {

  private static final String URL = "https://en.wikipedia.org/w/api.php";

  protected static JSONObject sendRequest(String request) {
    request = request.replaceAll(" ", "%20");
    StringBuilder jsonString = new StringBuilder();
    try {
      URL url = new URL(URL + request);
      HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Accept", "text/plain");
      connection.setRequestProperty("Content-Type", "multipart/form-data");
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        jsonString.append(line);
      }
      br.close();
      connection.disconnect();
    } catch (Exception ex) {
      // TODO Logging
      ex.printStackTrace();
    }
    return new JSONObject(jsonString.toString());
  }

  public static boolean invalidArticle(String article) {
    return !article.matches("^[^#<>\\[\\]|{}]+$");
  }

  protected static String encodeTitles(String title) {
    try {
      title = URLDecoder.decode(title, "UTF-8");
      return URLEncoder.encode(title, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return title;
    }
  }
}
