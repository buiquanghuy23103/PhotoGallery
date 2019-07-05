package com.example.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends JobService {
    public static final String SHOW_NOTIFICATION_ACTION = "photoGallery.PollService.SHOW_NOTIFICATION";
    private static final String TAG = "PollService";
    private static final long SERVICE_INTERVAL = TimeUnit.MINUTES.toMillis(1);
    private static final int POLL_SERVICE_REQUEST_CODE = 0;
    private static final int NOTIFICATION_ID = 0;
    private static final int JOB_ID = 1234;

    private PollTask mPollTask;

    public PollService() {
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mPollTask != null){
            mPollTask.cancel(true);
        }
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void>{
        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            pollPhotos();
            jobFinished(jobParameters[0], false);
            return null;
        }
    }

    public static boolean isServiceOn(Context context){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo:
             jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID){
                return true;
            }
        }
        return false;
    }

    public static void setServiceOn(Context context, boolean isOn){
        setServiceSchedule(context, isOn);
        Log.i(TAG, "Service is activated and put into schedule");
        QueryPreference.setServiceStatus(context, isOn);
    }

    private static void setServiceSchedule(Context context, boolean isOn) {
        ComponentName componentName = new ComponentName(context, PollService.class);
        JobInfo jobInfo = new  JobInfo.Builder(JOB_ID, componentName)
                .setPeriodic(SERVICE_INTERVAL) // 1 minute
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

        if (isOn){
            jobScheduler.schedule(jobInfo);
            Log.i(TAG, "Service scheduled");
        } else {
            jobScheduler.cancel(JOB_ID);
            Log.i(TAG, "Service is cancelled");
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
