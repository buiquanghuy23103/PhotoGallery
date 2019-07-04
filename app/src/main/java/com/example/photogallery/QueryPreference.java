package com.example.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreference {
    private static final String SEARCH_QUERY_PREF = "searchQuery";
    private static final String LAST_RESULT_ID_PREF = "lastResultId";

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

    public static String getLastResultIdPref(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LAST_RESULT_ID_PREF, "wrong");
    }

    public static void setLastResultIdPref(Context context, String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(LAST_RESULT_ID_PREF, lastResultId)
                .apply();
    }
}
