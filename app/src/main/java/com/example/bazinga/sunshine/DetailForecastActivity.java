package com.example.bazinga.sunshine;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailForecastActivity extends AppCompatActivity {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecast;

    private TextView mWeatherDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_forecast);

        mWeatherDisplay = (TextView)findViewById(R.id.display_weaterData);

        Intent intentThatStartedThisActivity = getIntent();

        if(intentThatStartedThisActivity != null){

            if(intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)){

                mForecast = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);

                mWeatherDisplay.setText(mForecast);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.detail,menu);

        MenuItem menuItem = menu.findItem(R.id.acion_share);

        menuItem.setIntent(createShareForecastIntent());

        return true;
    }

    private Intent createShareForecastIntent() {

        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecast + FORECAST_SHARE_HASHTAG)
                .getIntent();

        return shareIntent;

    }
}
