package com.example.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreference {
    private static final String SEARCH_QUERY_PREF = "searchQuery";
    private static final String LAST_RESULT_ID_PREF = "lastResultId";
    private static final String IS_ALARM_ON_PREF = "isAlarmOn";

    public static Boolean getServiceStatus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(IS_ALARM_ON_PREF, false);
    }

    public static void setServiceStatus(Context context, boolean isAlarmOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(IS_ALARM_ON_PREF, isAlarmOn)
                .apply();
    }

    public static String getSearchQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SEARCH_QUERY_PREF, "wrong");
    }

    public static void setSearchQuery(Context context, String searchQueryPref){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SEARCH_QUERY_PREF, searchQueryPref)
                .apply();
    }

    public static String getLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LAST_RESULT_ID_PREF, "wrong");
    }

    public static void setLastResultId(Context context, String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(LAST_RESULT_ID_PREF, lastResultId)
                .apply();
    }
}
