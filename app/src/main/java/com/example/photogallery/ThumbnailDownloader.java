package com.example.photogallery;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int DOWNLOAD_MESSAGE = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentHashMap<T, String> mQueue = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    public void queueThumbnail(T target, String url){
        // Add new target to the queue
        if (url == null){
            mQueue.remove(target);
        } else {
            mQueue.put(target, url);
        }

        mRequestHandler.obtainMessage(DOWNLOAD_MESSAGE, target)
                .sendToTarget();
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new PhotoHandler();
    }

    private class PhotoHandler extends Handler{
        public PhotoHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DOWNLOAD_MESSAGE){
                // Get message from main thread and do background work
                T target = (T) msg.obj;
                String url = mQueue.get(target);
                Bitmap bitmap = FlickrFetch.getBitmap(url);
                Log.i(TAG, "Bitmap created");

                // Send message back to main thread
                mResponseHandler.post(() -> {
                    if (mQueue.get(target) != url || mHasQuit){
                        return;
                    }

                    mQueue.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                });
            }
        }
    }

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }
}
