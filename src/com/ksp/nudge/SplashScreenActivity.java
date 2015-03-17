package com.ksp.nudge;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SplashScreenActivity extends Activity{

    private static int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                Intent homeActivity = new Intent(SplashScreenActivity.this,ActiveNudgesActivity.class);
                startActivity(homeActivity);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}
