package com.ksp.nudge.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.ksp.nudge.db.NudgeArrayAdapter;
import com.ksp.nudge.db.NudgeDatabaseHelper;
import com.ksp.nudge.model.Nudge;

import java.util.List;

import static com.ksp.nudge.R.layout.activity_splash_screen;


public class SplashScreenActivity extends Activity{

    private static int SPLASH_TIME_OUT = 3000;
    private boolean adapterSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_splash_screen);
        new GetActiveNudgesTask().execute(this);
        changeToActiveNudges();
    }

    private void changeToActiveNudges() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                while (!adapterSet) {
                }
                Intent homeActivity = new Intent(SplashScreenActivity.this,
                        ActiveNudgesActivity.class);
                startActivity(homeActivity);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    /**
     * Class that fetches active nudges from database asynchronously
     */
    private class GetActiveNudgesTask extends AsyncTask<Context,Void,List<Nudge>> {

        @Override
        protected List<Nudge> doInBackground(Context... contexts) {
            return NudgeDatabaseHelper.getPendingNudges();
        }

        protected void onPostExecute(List<Nudge> result){
            ActiveNudgesActivity.setNudgeAdapter(new NudgeArrayAdapter(SplashScreenActivity.this,
                    result));
            adapterSet = true;
        }
    }
}
