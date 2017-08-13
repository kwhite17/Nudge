package com.ksp.nudge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.ksp.database.NudgeDatabaseHelper;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static com.ksp.nudge.R.id.nudgeMessageText;
import static com.ksp.nudge.R.id.nudgeRecipientText;
import static com.ksp.nudge.R.id.nudgeSendDateText;
import static com.ksp.nudge.R.layout.active_nudge_item;
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
    private class GetActiveNudgesTask extends AsyncTask<Context,Void,Cursor> {

        @Override
        protected Cursor doInBackground(Context... contexts) {
            return new NudgeDatabaseHelper(contexts[0]).readMessagesFromDatabase();
        }

        protected void onPostExecute(Cursor result){
            int[] adapterColumns = new int[]{nudgeRecipientText,
                    nudgeMessageText, nudgeSendDateText};
            ActiveNudgesActivity.setNudgeAdapter(new NudgeCursorAdapter(getApplicationContext(),
                    active_nudge_item, result, result.getColumnNames(),
                    adapterColumns, FLAG_REGISTER_CONTENT_OBSERVER));
            adapterSet = true;
        }
    }
}
