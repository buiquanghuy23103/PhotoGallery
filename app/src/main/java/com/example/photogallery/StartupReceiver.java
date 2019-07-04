package com.example.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";


    public StartupReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receive broadcast intent " + intent.getAction());
        // Start service on boot
        boolean isServiceOn = QueryPreference.getServiceStatus(context);
        PollService.setServiceStatus(context, isServiceOn);
    }


}
