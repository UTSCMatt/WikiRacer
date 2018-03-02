package kappa.wikiracer.wiki;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

class SendRequest {

  private static final String URL = "https://en.wikipedia.org/w/api.php";

  protected static JSONObject sendRequest(String request, String method) {
    request = request.replaceAll(" ", "%20");
    StringBuffer jsonString = new StringBuffer();
    try {
      URL url = new URL(URL + request);
      HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setRequestMethod(method);
      connection.setRequestProperty("Accept", "text/plain");
      connection.setRequestProperty("Content-Type", "multipart/form-data");
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        jsonString.append(line);
      }
      br.close();
      connection.disconnect();
    } catch (Exception e) {
      // TODO Logging
      System.out.println(e);
    }
    return new JSONObject(jsonString.toString());
  }
}
