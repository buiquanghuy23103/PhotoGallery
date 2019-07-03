package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
    private static final String FETCH_RECENT_METHODS = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri.parse(BASE_URL).buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    private static Uri mRequestUri;

    // TODO: use Volley to fetch JSON from URL
    // TODO: use Glide to load image

    public static List<Photo> getRecentPhotos(){
        mRequestUri = ENDPOINT.buildUpon()
                .appendQueryParameter("method", FETCH_RECENT_METHODS).build();
        return getGallery();
    }

    public static List<Photo> getSearchPhotos(String query){
        mRequestUri = ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("query", query)
                .build();
        return getGallery();
    }

    public static Bitmap getBitmap(String url){
        try {
            byte[] bitmapBytes = getUrlBytes(url);
            return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        } catch (IOException e){
            Log.e(TAG, "Failed to load bitmap from url " + e);
            return null;
        }
    }

    private static byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private static List<Photo> getGallery(){
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
            URL url = new URL(mRequestUri.toString());
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
}
