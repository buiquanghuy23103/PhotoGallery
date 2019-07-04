package com.example.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(PollService.SHOW_NOTIFICATION_ACTION);
        getActivity().registerReceiver(mOnShowNotification, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Got broadcast: " + intent.getAction(), Toast.LENGTH_SHORT)
            .show();
        }
    };
}
