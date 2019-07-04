package com.example.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreference {
    private static final String SEARCH_QUERY_PREF = "searchQuery";

    public static String getSearchQueryPref(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SEARCH_QUERY_PREF, "wrong");
    }

    public static void setSearchQueryPref(Context context, String searchQueryPref){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SEARCH_QUERY_PREF, searchQueryPref)
                .apply();
    }
}
