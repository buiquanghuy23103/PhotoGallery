package com.example.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mRecyclerView;
    private List<Photo> photoGallery;
    private ThumbnailDownloader<GalleryAdapter.ItemHolder> mThumbnailDownloader;

    // TODO: create a unit test to check if photoGallery is null in setUpAdapter()
    public PhotoGalleryFragment(){}

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchGallery().execute();

        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setUpAdapter();

        return view;
    }


    private void setUpAdapter(){
        if (isAdded()){
            GalleryAdapter adapter = new GalleryAdapter(photoGallery);
            mRecyclerView.setAdapter(adapter);
        }
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ItemHolder>{
        private List<Photo> mPhotos;

        public GalleryAdapter(List<Photo> photos) {
            mPhotos = photos;
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.holder_photo, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            Photo photo = mPhotos.get(position);
            holder.bind(photo);
        }

        @Override
        public int getItemCount() {
            return mPhotos==null? 0 : mPhotos.size();
        }

        private class ItemHolder extends RecyclerView.ViewHolder{
            private ImageView mImageView;

            public ItemHolder(@NonNull View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.photo_view);
            }

            public void bind(Photo photo){
                mThumbnailDownloader.queueThumbnail(this, photo.getUrl());
                Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close,
                        getActivity().getTheme());
                mImageView.setImageDrawable(placeholder);
            }
        }
    }

    private class FetchGallery extends AsyncTask<Void, Void, List<Photo>>{
        @Override
        protected List<Photo> doInBackground(Void... voids) {

            return FlickrFetch.getGallery();
        }

        @Override
        protected void onPostExecute(List<Photo> photos) {
            photoGallery = photos;
            setUpAdapter();
        }

    }
}
