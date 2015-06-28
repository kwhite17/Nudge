package com.ksp.nudge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static com.ksp.nudge.R.id.splashScreenAdView;
import static com.ksp.nudge.R.layout.activity_splash_screen;


public class SplashScreenActivity extends Activity{

    private static int SPLASH_TIME_OUT = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_splash_screen);
        AdView aboutUsAdView = (AdView) findViewById(splashScreenAdView);
        aboutUsAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                changeToActiveNudges();

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                changeToActiveNudges();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                changeToActiveNudges();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                changeToActiveNudges();

            }
        });
        aboutUsAdView.loadAd(new AdRequest.Builder().build());
    }

    private void changeToActiveNudges() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeActivity = new Intent(SplashScreenActivity.this,
                        ActiveNudgesActivity.class);
                startActivity(homeActivity);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
