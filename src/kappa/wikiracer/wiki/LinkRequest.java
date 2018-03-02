package kappa.wikiracer.wiki;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class LinkRequest {
	public static String sendRequest(String article) {
		String request = "?action=query&limit=max&format=json&prop=links&titles=" + article;
		List<String> titlesAsJson = continueRequest(request);
		// After this is testing purposes
		StringBuffer titles = new StringBuffer();
		for (String title : titlesAsJson) {
			titles.append(title + "\n");
		}
		return titles.toString();
	}

	private static List<String> continueRequest(String rawRequest) {
		List<String> titles = new ArrayList<>();
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
				titles.add(links.getJSONObject(i).getString(MediaWikiConstants.TITLE));
			}
		} while (continueJson != null);
		return titles;
	}
}
