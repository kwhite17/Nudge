package com.ksp.nudge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static android.content.Intent.ACTION_VIEW;
import static com.ksp.nudge.R.array.urlArray;
import static com.ksp.nudge.R.id.aboutUsAdView;
import static com.ksp.nudge.R.id.portfolioButton;


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
        AdView adView = (AdView) findViewById(aboutUsAdView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    public void launchBrowser(View v) {
        String[] launchUrls = getResources().getStringArray(urlArray);
        Intent browserIntent = new Intent(ACTION_VIEW);
        if (v.getId() == portfolioButton) {
            browserIntent.setData(Uri.parse(launchUrls[0]));
        } else {
            browserIntent.setData(Uri.parse(launchUrls[1]));
        }
        startActivity(browserIntent);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ActiveNudgesActivity.class));
        finish();
    }
}
