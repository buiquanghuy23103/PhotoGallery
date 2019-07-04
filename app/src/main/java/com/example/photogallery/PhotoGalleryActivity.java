package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PhotoGalleryActivity extends BaseActivity {
    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    protected Fragment getFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
