package com.dape.newsapp.utils;

import android.text.TextUtils;
import android.util.Log;

import com.dape.newsapp.R;
import com.dape.newsapp.model.News;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danale on 01/03/2018.
 */


@SuppressWarnings("ALL")
public final class NewsQueryUtils {
    private static final String LOG_TAG = NewsQueryUtils.class.getSimpleName();
    private static final String TAG_RESPONSE = "response";
    private static final String TAG_RESULTS = "results";
    private static final String TAG_PILLAR_NAME = "pillarName";
    private static final String TAG_SECTION_NAME = "sectionName";
    private static final String TAG_WEB_PUBLICATION_DATE = "webPublicationDate";
    private static final String TAG_WEB_TITLE = "webTitle";
    private static final String TAG_WEB_URL = "webUrl";
    private static final String TAG_TAGS = "tags";
    private static final String TAG_AUTHOR_WEB_TITLE = "webTitle";
    private static final String TAG_REQUEST_METHOD = "GET";
    private static final String CHARSET = "UTF-8";
    private static final int TAG_READ_TIMEOUT = 10000;
    private static final int TAG_CONNECT_TIMEOUT = 15000;
    private static final int TAG_RESPONSE_CODE = 200;
    private NewsQueryUtils() {
    }
    public static JsonObject getTags (String requestUrl){
        URL url = createUrl(requestUrl);
        JsonObject jsonResponse = null;
        try {
            String str = makeHttpRequest(url);
            jsonResponse = new JsonParser().parse(str).getAsJsonObject();
        } catch (IOException e){
            Log.e(LOG_TAG, String.valueOf(R.string.log_problem_request), e);
        }

        return jsonResponse;
    }
    public static List<News> takeNewsData (String requestUrl){
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.log_problem_request), e);
        }
        return extractFeatureFromJSON(jsonResponse);
    }
    private static List<News> extractFeatureFromJSON(String newsJSON) {
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }
        List<News> listNews = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(newsJSON);
            JSONObject jsonResults = jsonObj.getJSONObject(TAG_RESPONSE);
            JSONArray results = jsonResults.getJSONArray(TAG_RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject currentObject = results.getJSONObject(i);
                String pillarName = currentObject.getString(TAG_PILLAR_NAME);
                String sectionName = "";
                if (currentObject.has(TAG_SECTION_NAME)) {
                    sectionName = currentObject.getString(TAG_SECTION_NAME);
                }
                String webPublicationDate = currentObject.getString(TAG_WEB_PUBLICATION_DATE);
                String webTitle = currentObject.getString(TAG_WEB_TITLE);
                String webUrl = currentObject.getString(TAG_WEB_URL);
                String author;
                List<String> authorList = new ArrayList<>();
                JSONArray tags = currentObject.getJSONArray(TAG_TAGS);
            for (int y = 0; y < tags.length(); y++) {
                JSONObject jsonTags = tags.getJSONObject(y);
                String name = jsonTags.getString(TAG_AUTHOR_WEB_TITLE);
                authorList.add(name);
            }
                if (authorList.size() == 0){
                    author = "-";
                }else{
                    author = TextUtils.join(", ",authorList);
                }
                News news = new News(pillarName, sectionName, webPublicationDate, webTitle, webUrl, author);
                listNews.add(news);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.log_parsing), e);
        }
        return listNews;
    }
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        //noinspection finally
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(TAG_REQUEST_METHOD);
            urlConnection.setReadTimeout(TAG_READ_TIMEOUT);
            urlConnection.setConnectTimeout(TAG_CONNECT_TIMEOUT);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == TAG_RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, String.valueOf(R.string.log_error_response) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.log_problem_retrieving), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return jsonResponse;
        }
    }
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(CHARSET));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
    private static URL createUrl(String strUrl) {
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, String.valueOf(R.string.log_problem_building_url), e);
        }
        return url;
    }
}
