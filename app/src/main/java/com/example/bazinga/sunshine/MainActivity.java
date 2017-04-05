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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bazinga.sunshine.data.SunshinePreferences;
import com.example.bazinga.sunshine.utilities.NetworkUtils;
import com.example.bazinga.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.bazinga.sunshine.utilities.SunshineDateUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler ,LoaderManager.LoaderCallbacks{

    private static final String TAG = "MainAcitivity";

    private RecyclerView mRecyclerView;

    private ForecastAdapter mForecastAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private static final String SEARCH_QUERY_URL_EXTRA = "location"; // 用于给 AsyncLoader 传递数值的键

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);

        mRecyclerView.setAdapter(mForecastAdapter);

        loadWeatherData();
    }

    private void loadWeatherData() {

        String location = SunshinePreferences.getPreferredWeatherLocation(this);

        new FetchWeatherTask().execute(location);
    }

    @Override
    public void onClickItem(String s) {

        if (s != null){

            Intent intent = new Intent(this,DetailForecastActivity.class);

            intent.putExtra(Intent.EXTRA_TEXT,s);

            startActivity(intent);

        }

    }

    @Override
    public Loader onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {

            @Override
            protected void onStartLoading() {

                if (args == null)

                    return;

                mLoadingIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public String[] loadInBackground() {

                String location =  args.getString(SEARCH_QUERY_URL_EXTRA);

                if (location == null || TextUtils.isEmpty(location))

                    return null;
                URL weatherRequestUrl = NetworkUtils.buildUrl(location);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;

                } catch (Exception e) {

                    e.printStackTrace();

                    return null;
                }


            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    // TODO (8) Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData

    // TODO (5) Create a class that extends AsyncTask to perform network requests

    class FetchWeatherTask extends AsyncTask<String,Void,String []>{

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0)

                return null;

            String location = params[0];

            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try {
                String jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                return simpleJsonWeatherData;

            } catch (Exception e) {

                e.printStackTrace();

                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] weatherData) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (weatherData != null) {

                showWeatherDataView();

                mForecastAdapter.setDatas(weatherData);

            } else {
                showErrorMessage();
            }
        }
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

            mForecastAdapter.setDatas(null);

            loadWeatherData();

            return true;
        }

        if(id == R.id.acion_map){

            openLocationInMap();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openLocationInMap() {

        String addressString = "1600 Ampitheatre Parkway, CA";

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
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}