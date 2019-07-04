package com.example.photogallery;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

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
        setHasOptionsMenu(true);

        updateGalleryUrl();
        startPollService();
        downloadGallery();
    }

    private void startPollService() {
        PollService.setServiceStatus(getActivity(), true);
    }

    private void downloadGallery() {
        Handler responsHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responsHandler);
        mThumbnailDownloader.setThumbnailDownloadListener((itemHolder, bitmap) -> {
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            itemHolder.bindImageView(drawable);
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    private void updateGalleryUrl() {
        String query = QueryPreference.getSearchQuery(getActivity());
        new FetchGallery().execute(query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_search:
                QueryPreference.setSearchQuery(getActivity(), null);
                return true;

            case R.id.toggle_service:
                boolean serviceState = !PollService.isServiceOn(getActivity());
                PollService.setServiceStatus(getActivity(), serviceState);
                getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        setUpSearchView(menu);
        MenuItem item = menu.findItem(R.id.toggle_service);
        int stringId = PollService.isServiceOn(getActivity())?
                R.string.stop_polling : R.string.start_polling;
        item.setTitle(stringId);
    }

    private void setUpSearchView(Menu menu) {
        MenuItem item = menu.findItem(R.id.search_view);
        final SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "Query text submitted");
                QueryPreference.setSearchQuery(getActivity(), query);
                hideKeyboard();
                updateGalleryUrl();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "Query text change" + newText);
                return true;
            }
        });

        searchView.setOnSearchClickListener(view -> {
            String query = QueryPreference.getSearchQuery(getActivity());
            searchView.setQuery(query, false);
        });
    }

    private void hideKeyboard(){
        final InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearUp();
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
                bindImageView(placeholder);
            }

            public void bindImageView(Drawable drawable){
                mImageView.setImageDrawable(drawable);
            }
        }
    }

    private class FetchGallery extends AsyncTask<String, Void, List<Photo>>{
        @Override
        protected List<Photo> doInBackground(String... strings) {
            return FlickrFetch.getGalleryByQuery(strings[0]);
        }

        @Override
        protected void onPostExecute(List<Photo> photos) {
            photoGallery = photos;
            setUpAdapter();
        }
    }
}
