package com.example.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    public static final String SHOW_NOTIFICATION_ACTION = "photoGallery.PollService.SHOW_NOTIFICATION";
    private static final String TAG = "PollService";
    private static final long SERVICE_INTERVAL = TimeUnit.MINUTES.toMillis(1);
    private static final int POLL_SERVICE_REQUEST_CODE = 0;
    private static final int NOTIFICATION_ID = 0;

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static boolean isServiceOn(Context context){
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                POLL_SERVICE_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    public static void setServiceOn(Context context, boolean isOn){
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, POLL_SERVICE_REQUEST_CODE,
                intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    SERVICE_INTERVAL,
                    pendingIntent);
            Log.i(TAG, "Service is on");
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        QueryPreference.setServiceStatus(context, true);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (isNetworkConnected()){
            pollPhotos();
        }
    }

    private void pollPhotos() {
        String query = QueryPreference.getSearchQuery(this);
        List<Photo> gallery = FlickrFetch.getGalleryByQuery(query);

        String lastResultId = QueryPreference.getLastResultId(this);
        String resultId = gallery.get(0).getId();
        if (!resultId.equals(lastResultId)){
            popUpNotification(getNotification());
        }

        QueryPreference.setLastResultId(this, lastResultId);
        sendBroadcast(new Intent(SHOW_NOTIFICATION_ACTION));
    }

    private void popUpNotification(Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification getNotification() {
        Intent i = PhotoGalleryActivity.newIntent(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                POLL_SERVICE_REQUEST_CODE,
                i,
                0);

        String title = getResources().getString(R.string.new_pic_title);
        return new  NotificationCompat.Builder(this, TAG)
                .setTicker(title)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(title)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().isConnected();
    }
}
