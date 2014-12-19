package jk.kamoru.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import jk.kamoru.crazy.CRAZY;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Google Image provider
 * @author kamoru
 *
 */
@Slf4j
public class GoogleImageProvider {

	final static String URLPATTERN = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&userip=&safe=off";
	final static String REFERER = "http://www.kamoru.com";
	
	/**
	 * 주어진 쿼리로 구글 이미지 검색을 사용해 URL list로 반환.<br>
	 * google url : https://ajax.googleapis.com/ajax/services/search/images<br>
	 * 에러 발생시 빈 list 리턴.
	 * 
	 * @param query
	 * @return list of url
	 */
	public static List<URL> search(String query) {
		List<URL> list = new ArrayList<URL>();
		BufferedReader reader = null;
		try {
			// connect
			URL url = new URL(String.format(URLPATTERN, URLEncoder.encode(query, CRAZY.CHARSET)));
			log.debug("search : {}", url);
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer", REFERER);

			// read response
			StringBuilder response = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), CRAZY.URL_ENCODING));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			// parsing json
			JSONObject json = JSONObject.fromObject(response.toString());
			JSONObject responseData = json.getJSONObject("responseData");
			JSONArray results = responseData.getJSONArray("results");
			
			// make list
			for (int i = 0, e = results.size(); i < e; i++) {
				String urlStr = results.getJSONObject(i).getString("url");
				list.add(new URL(urlStr));
			}
			log.debug("find %s images", list.size());
		} catch (JSONException jsone) {
			log.error("json error", jsone);
		} catch (Exception e) {
			log.error("Fail to find image url on google", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					log.error("reader close error", e);
				}
		}
		return list;
	}

	
	public static void main(String[] args) {
		for (URL url : GoogleImageProvider.search("kamoru")) 
			System.out.println(url);
	}
	
}
