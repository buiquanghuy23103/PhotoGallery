package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlickrFetch {
    private static final String TAG = "FlickrFetch";

    private static final String BASE_URL = "https://www.flickr.com/services/rest/";
    private static final String API_KEY = "7e6ca88d2b6b5730594ff32c5bea0579";

    // TODO: use Retrofit to fetch JSON from URL
    public static List<Photo> getGallery(){
        List<Photo> photos = null;
        Gson gson = new Gson();
        if (getJsonArray().length() > 0){
            photos = Arrays.asList(gson.fromJson(getJsonArray().toString(), Photo[].class));
        }

        for (int i = 0; i < 5; i++) {
            Log.i(TAG, "photo url: " + photos.get(i).getUrl());
        }

        return photos;
    }

    private static JSONArray getJsonArray(){
        try {
            JSONObject jsonBody = new JSONObject(getJsonString());
            JSONObject root = jsonBody.getJSONObject("photos");
            return root.getJSONArray("photo");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get JSON object from String" + e);
            e.printStackTrace();
            return null;
        }
    }

    private static String getJsonString(){
        String jsonString = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(getRequestUri().toString());
            connection = (HttpURLConnection) url.openConnection();

            InputStream input = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            String line = null;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null){
                builder.append(line);
                builder.append("\n");
            }

            jsonString = builder.toString();
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL exception " + e);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Failed to get Http URL connection from URL " + e);
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null){
                connection.disconnect();
            }

            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Confirm whether JSON is fetched.
        Log.i(TAG, jsonString);

        return jsonString;
    }

    private static Uri getRequestUri(){
        return Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("method", "flickr.photos.getRecent")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s")
                .build();
    }
}
