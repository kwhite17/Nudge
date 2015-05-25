package com.ksp.nudge;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class AboutNudgeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_nudge);
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadAd();
    }

    private void loadAd() {
        AdView aboutUsAdView = (AdView) findViewById(R.id.aboutUsAdView);
        aboutUsAdView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ActiveNudgesActivity.class));
    }
}
