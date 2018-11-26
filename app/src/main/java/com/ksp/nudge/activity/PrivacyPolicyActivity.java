package com.ksp.nudge.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ksp.nudge.R;

import androidx.appcompat.app.AppCompatActivity;


public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ActiveNudgesActivity.class));
        finish();
    }
}
