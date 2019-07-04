package com.example.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final long SERVICE_INTERVAL = TimeUnit.MINUTES.toMillis(1);
    private static final int POLL_SERVICE_REQUEST_CODE = 0;

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                POLL_SERVICE_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    public static void setAlarm(Context context, boolean isOn){
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, POLL_SERVICE_REQUEST_CODE,
                intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    SERVICE_INTERVAL,
                    pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (isNetworkConnected()){
            String query = QueryPreference.getSearchQueryPref(this);
            String lastResultId = QueryPreference.getLastResultIdPref(this);
            List<Photo> gallery = FlickrFetch.getGalleryByQuery(query);

            String resultId = gallery.get(0).getId();
            if (resultId.equals(lastResultId)){
                Log.i(TAG, "Old result " + resultId);
            } else {
                Log.i(TAG, "New result " + resultId);
            }
            QueryPreference.setLastResultIdPref(this, lastResultId);
        }
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().isConnected();
    }
}
