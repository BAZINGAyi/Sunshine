/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bazinga.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bazinga.sunshine.data.SunshinePreferences;
import com.example.bazinga.sunshine.data.WeatherContract;
import com.example.bazinga.sunshine.sync.SunshineSyncUtils;
import com.example.bazinga.sunshine.utilities.FakeDataUtils;
import com.example.bazinga.sunshine.utilities.NetworkUtils;
import com.example.bazinga.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler ,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MainAcitivity";

    private int mPosition = RecyclerView.NO_POSITION;

    private RecyclerView mRecyclerView;

    private ForecastAdapter mForecastAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private static final String SEARCH_QUERY_URL_EXTRA = "location"; // 用于给 AsyncLoader 传递数值的键

    private static final int ID_FORECAST_LOADER = 44; // 用于将 activity 于 Loder 绑定

    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);


        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this,this);

        mRecyclerView.setAdapter(mForecastAdapter);

        showLoading();

         /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */
        int loaderId = ID_FORECAST_LOADER;

        /*
         * From MainActivity_Before, we have implemented the LoaderCallbacks interface with the type of
         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */
        LoaderManager.LoaderCallbacks<Cursor> callback = MainActivity.this;

        /*
         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
         * to.
         */
        Bundle bundleForLoader = null;

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */

        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);

        Log.d(TAG, "onCreate: registering preference changed listener");

       // loadWeatherData();

        Log.d(TAG, "onCreate: registering preference changed listener");

        /*
         * Register MainActivity_Before as an OnPreferenceChangedListener to receive a callback when a
         * SharedPreference has changed. Please note that we must unregister MainActivity_Before as an
         * OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
         */
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // 生成假数据
        //FakeDataUtils.insertFakeData(this);

        //  不要每次打开都进行同步
       // SunshineSyncUtils.startImmediateSync(this);

        SunshineSyncUtils.initialize(this);
    }




    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

//      COMPLETED (23) Remove the onStartLoading method declaration
//      COMPLETED (24) Remove the loadInBackground method declaration
//      COMPLETED (25) Remove the deliverResult method declaration

        switch (loaderId) {

//          COMPLETED (22) If the loader requested is our forecast loader, return the appropriate CursorLoader
            case ID_FORECAST_LOADER:

                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
//      COMPLETED (29) If mPosition equals RecyclerView.NO_POSITION, set it to 0
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
//      COMPLETED (30) Smooth scroll the RecyclerView to mPosition
        mRecyclerView.smoothScrollToPosition(mPosition);

//      COMPLETED (31) If the Cursor's size is not equal to 0, call showWeatherDataView
        if (data.getCount() != 0) showWeatherDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /*
         * Set this flag to true so that when control returns to MainActivity_Before, it can refresh the
         * data.
         *
         * This isn't the ideal solution because there really isn't a need to perform another
         * GET request just to change the units, but this is the simplest solution that gets the
         * job done for now. Later in this course, we are going to show you more elegant ways to
         * handle converting the units from celsius to fahrenheit and back without hitting the
         * network again by keeping a copy of the data in a manageable format.
         */
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }

    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailForecastActivity.class);
//      COMPLETED (39) Refactor onClick to pass the URI for the clicked date with the Intent
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id =item.getItemId();

        if(id  == R.id.acion_fresh){

            invalidateData();

            getSupportLoaderManager().restartLoader(ID_FORECAST_LOADER, null, this);

            return true;
        }

        if(id == R.id.acion_map){

            openLocationInMap();

            return true;
        }

        if (id == R.id.action_settings) {

            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);

            startActivity(startSettingsActivity);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openLocationInMap() {

        String addressString = SunshinePreferences.getPreferredWeatherLocation(this);

        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {

            startActivity(intent);

        } else {

            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }

    }

    private void showWeatherDataView() {
          /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void invalidateData() {

        mForecastAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Unregister MainActivity_Before as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
         * If the preferences for location or units have changed since the user was last in
         * MainActivity_Before, perform another query and set the flag to false.
         *
         * This isn't the ideal solution because there really isn't a need to perform another
         * GET request just to change the units, but this is the simplest solution that gets the
         * job done for now. Later in this course, we are going to show you more elegant ways to
         * handle converting the units from celsius to fahrenheit and back without hitting the
         * network again by keeping a copy of the data in a manageable format.
         */
        if (PREFERENCES_HAVE_BEEN_UPDATED) {

            Log.d(TAG, "onStart: preferences were updated");

            getSupportLoaderManager().restartLoader(ID_FORECAST_LOADER, null, this);

            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    private void showLoading() {
        /* Then, hide the weather data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

}