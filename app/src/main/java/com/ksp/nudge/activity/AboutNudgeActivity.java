package com.ksp.nudge.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.ksp.nudge.R;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Intent.ACTION_VIEW;


public class AboutNudgeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_nudge);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }


    public void launchBrowser(View v) {
        String[] launchUrls = getResources().getStringArray(R.array.url_array);
        Intent browserIntent = new Intent(ACTION_VIEW);
        if (v.getId() == R.id.portfolioButton) {
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
