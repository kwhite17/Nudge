package com.ksp.nudge;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class SplashScreenActivity extends Activity{

    private static int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
//        AdView aboutUsAdView = (AdView) findViewById(R.id.splashScreenAdView);
//        aboutUsAdView.loadAd(new AdRequest.Builder().build());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeActivity = new Intent(SplashScreenActivity.this, ActiveNudgesActivity.class);
                startActivity(homeActivity);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    /**
     * Class that loads ads asynchronously to minimize performance hit
     */
    private class LoadAdAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... adId) {
            AdView aboutUsAdView = (AdView) findViewById(adId[0]);
            aboutUsAdView.loadAd(new AdRequest.Builder().build());
            return null;
        }
    }
}
