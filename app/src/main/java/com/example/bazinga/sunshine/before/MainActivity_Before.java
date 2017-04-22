///*
// * Copyright (C) 2016 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.example.bazinga.sunshine.before;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.AsyncTaskLoader;
//import android.support.v4.content.Loader;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.preference.PreferenceManager;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import com.example.bazinga.sunshine.DetailForecastActivity;
//
//import com.example.bazinga.sunshine.R;
//import com.example.bazinga.sunshine.SettingsActivity;
//import com.example.bazinga.sunshine.data.SunshinePreferences;
//import com.example.bazinga.sunshine.utilities.NetworkUtils;
//import com.example.bazinga.sunshine.utilities.OpenWeatherJsonUtils;
//
//import java.net.URL;
//
//public class MainActivity_Before extends AppCompatActivity implements
//        ForecastAdapter.ForecastAdapterOnClickHandler,
//        LoaderManager.LoaderCallbacks<String[]>,
//        SharedPreferences.OnSharedPreferenceChangeListener{
//
//    private static final String TAG = "MainAcitivity";
//
//    private RecyclerView mRecyclerView;
//
//    private ForecastAdapter mForecastAdapter;
//
//    private TextView mErrorMessageDisplay;
//
//    private ProgressBar mLoadingIndicator;
//
//    private static final String SEARCH_QUERY_URL_EXTRA = "location"; // 用于给 AsyncLoader 传递数值的键
//
//    private static final int WEATHER_SEARCH_LOADER = 22;
//
//    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_forecast);
//
//        /*
//         * Using findViewById, we get a reference to our TextView from xml. This allows us to
//         * do things like set the text of the TextView.
//         */
//
//        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
//
//        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
//
//        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
//
//        LinearLayoutManager layoutManager
//                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//
//        mRecyclerView.setLayoutManager(layoutManager);
//
//        mRecyclerView.setHasFixedSize(true);
//
//        mForecastAdapter = new ForecastAdapter(this);
//
//        mRecyclerView.setAdapter(mForecastAdapter);
//
//         /*
//         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
//         * on our Loader at a later point in time through the support LoaderManager.
//         */
//        int loaderId = WEATHER_SEARCH_LOADER;
//
//        /*
//         * From MainActivity_Before, we have implemented the LoaderCallbacks interface with the type of
//         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
//         * to the call to initLoader below. This means that whenever the loaderManager has
//         * something to notify us of, it will do so through this callback.
//         */
//        LoaderManager.LoaderCallbacks<String[]> callback = MainActivity_Before.this;
//
//        /*
//         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
//         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
//         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
//         * to.
//         */
//        Bundle bundleForLoader = null;
//
//        /*
//         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
//         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
//         * the last created loader is re-used.
//         */
//
//        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
//
//        Log.d(TAG, "onCreate: registering preference changed listener");
//
//       // loadWeatherData();
//
//        Log.d(TAG, "onCreate: registering preference changed listener");
//
//        /*
//         * Register MainActivity_Before as an OnPreferenceChangedListener to receive a callback when a
//         * SharedPreference has changed. Please note that we must unregister MainActivity_Before as an
//         * OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
//         */
//        PreferenceManager.getDefaultSharedPreferences(this)
//                .registerOnSharedPreferenceChangeListener(this);
//    }
//
//    private void loadWeatherData() {
//
//        String location = SunshinePreferences.getPreferredWeatherLocation(this);
//
//        new FetchWeatherTask().execute(location);
////
////        Bundle queryBundle = new Bundle();
////
////        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, location);
////
////        LoaderManager loaderManager = getSupportLoaderManager();
////
////        Loader<String[]> WeatherSearchLoader = loaderManager.getLoader(WEATHER_SEARCH_LOADER);
////
////        loaderManager.initLoader(WEATHER_SEARCH_LOADER, queryBundle, this);
//
////        if (WeatherSearchLoader == null) {
////
////            loaderManager.initLoader(WEATHER_SEARCH_LOADER, queryBundle, this);
////
////        } else {
////
////            loaderManager.restartLoader(WEATHER_SEARCH_LOADER, queryBundle, this);
////        }
//    }
//
//    @Override
//    public void onClickItem(String s) {
//
//        if (s != null){
//
//            Intent intent = new Intent(this,DetailForecastActivity.class);
//
//            intent.putExtra(Intent.EXTRA_TEXT,s);
//
//            startActivity(intent);
//
//        }
//
//    }
//
//    @Override
//    public Loader<String[]>  onCreateLoader(int id, final Bundle args) {
//        return new AsyncTaskLoader<String[]>(this) {
//
//            /* This String array will hold and help cache our weather data */
//            String[] mWeatherData = null;
//
//            @Override
//            protected void onStartLoading() {
//
//                if (mWeatherData != null) { // 当旋转屏幕时可以和 AdyncTask 对比比较
//
//                    deliverResult(mWeatherData);
//
//                } else {
//
//                    mLoadingIndicator.setVisibility(View.VISIBLE);
//
//                    forceLoad();   // 不写这个会导致 ui 一直加载
//                }
//
//            }
//
//            @Override
//            public String[] loadInBackground() {
//
//                String location = SunshinePreferences
//                        .getPreferredWeatherLocation(MainActivity_Before.this);
//
//                if (location == null || TextUtils.isEmpty(location))
//
//                    return null;
//                URL weatherRequestUrl = NetworkUtils.buildUrl(location);
//
//                try {
//                    String jsonWeatherResponse = NetworkUtils
//                            .getResponseFromHttpUrl(weatherRequestUrl);
//
//                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
//                            .getSimpleWeatherStringsFromJson(MainActivity_Before.this, jsonWeatherResponse);
//
//                    return simpleJsonWeatherData;
//
//                } catch (Exception e) {
//
//                    e.printStackTrace();
//
//                    return null;
//                }
//
//
//            }
//
//            public void deliverResult(String[] data) {
//
//                mWeatherData = data;
//
//                super.deliverResult(data);
//            }
//        };
//    }
//
//    @Override
//    public void onLoadFinished(Loader<String[]> loader, String[] weatherData) {
//
//        mLoadingIndicator.setVisibility(View.INVISIBLE);
//
//        mForecastAdapter.setDatas(weatherData);
//
//        if(weatherData == null) {
//
//            showErrorMessage();
//
//            return;
//
//        }else {
//
//            showWeatherDataView();
//
//        }
//
//    }
//
//    @Override
//    public void onLoaderReset(Loader<String[]> loader) {
//
//    }
//
//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            /*
//         * Set this flag to true so that when control returns to MainActivity_Before, it can refresh the
//         * data.
//         *
//         * This isn't the ideal solution because there really isn't a need to perform another
//         * GET request just to change the units, but this is the simplest solution that gets the
//         * job done for now. Later in this course, we are going to show you more elegant ways to
//         * handle converting the units from celsius to fahrenheit and back without hitting the
//         * network again by keeping a copy of the data in a manageable format.
//         */
//        PREFERENCES_HAVE_BEEN_UPDATED = true;
//    }
//
//
//
//    // TODO (8) Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData
//
//    // TODO (5) Create a class that extends AsyncTask to perform network requests
//
//    class FetchWeatherTask extends AsyncTask<String,Void,String []>{
//
//        @Override
//        protected void onPreExecute() {
//
//            super.onPreExecute();
//
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected String[] doInBackground(String... params) {
//
//            if (params.length == 0)
//
//                return null;
//
//            String location = params[0];
//
//            URL weatherRequestUrl = NetworkUtils.buildUrl(location);
//
//            try {
//                String jsonWeatherResponse = NetworkUtils
//                        .getResponseFromHttpUrl(weatherRequestUrl);
//
//                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
//                        .getSimpleWeatherStringsFromJson(MainActivity_Before.this, jsonWeatherResponse);
//
//                return simpleJsonWeatherData;
//
//            } catch (Exception e) {
//
//                e.printStackTrace();
//
//                return null;
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(String[] weatherData) {
//
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//
//            if (weatherData != null) {
//
//                showWeatherDataView();
//
//                mForecastAdapter.setDatas(weatherData);
//
//            } else {
//                showErrorMessage();
//            }
//        }
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.menu, menu);
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id =item.getItemId();
//
//        if(id  == R.id.acion_fresh){
//
//            invalidateData();
//
//            getSupportLoaderManager().restartLoader(WEATHER_SEARCH_LOADER, null, this);
//
//            return true;
//        }
//
//        if(id == R.id.acion_map){
//
//            openLocationInMap();
//
//            return true;
//        }
//
//        if (id == R.id.action_settings) {
//
//            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
//
//            startActivity(startSettingsActivity);
//
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void openLocationInMap() {
//
//        String addressString = SunshinePreferences.getPreferredWeatherLocation(this);
//
//        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//
//        intent.setData(geoLocation);
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//
//            startActivity(intent);
//
//        } else {
//
//            Log.d(TAG, "Couldn't call " + geoLocation.toString()
//                    + ", no receiving apps installed!");
//        }
//
//    }
//
//    private void showWeatherDataView() {
//        /* First, make sure the error is invisible */
//        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
//        /* Then, make sure the weather data is visible */
//        mRecyclerView.setVisibility(View.VISIBLE);
//    }
//
//
//    private void showErrorMessage() {
//        /* First, hide the currently visible data */
//        mRecyclerView.setVisibility(View.INVISIBLE);
//        /* Then, show the error */
//        mErrorMessageDisplay.setVisibility(View.VISIBLE);
//    }
//
//    private void invalidateData() {
//
//        mForecastAdapter.setDatas(null);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        /* Unregister MainActivity_Before as an OnPreferenceChangedListener to avoid any memory leaks. */
//        PreferenceManager.getDefaultSharedPreferences(this)
//                .unregisterOnSharedPreferenceChangeListener(this);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        /*
//         * If the preferences for location or units have changed since the user was last in
//         * MainActivity_Before, perform another query and set the flag to false.
//         *
//         * This isn't the ideal solution because there really isn't a need to perform another
//         * GET request just to change the units, but this is the simplest solution that gets the
//         * job done for now. Later in this course, we are going to show you more elegant ways to
//         * handle converting the units from celsius to fahrenheit and back without hitting the
//         * network again by keeping a copy of the data in a manageable format.
//         */
//        if (PREFERENCES_HAVE_BEEN_UPDATED) {
//
//            Log.d(TAG, "onStart: preferences were updated");
//
//            getSupportLoaderManager().restartLoader(WEATHER_SEARCH_LOADER, null, this);
//
//            PREFERENCES_HAVE_BEEN_UPDATED = false;
//        }
//    }
//
//}