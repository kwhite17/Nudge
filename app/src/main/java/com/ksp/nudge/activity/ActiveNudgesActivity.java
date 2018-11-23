package com.ksp.nudge.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.ksp.nudge.R;
import com.ksp.nudge.db.NudgeArrayAdapter;
import com.ksp.nudge.db.NudgeDatabaseHelper;


public class ActiveNudgesActivity extends AppCompatActivity {

    private static NudgeArrayAdapter nudgeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_nudges);

        findViewById(R.id.newNudgeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActiveNudgesActivity.this, MessageFormActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //build the list of active nudges
        ((ListView) findViewById(R.id.activeNudgeList)).setEmptyView(findViewById(R.id.instructionText));
        new GetActiveNudgesTask().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_active_nudges, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.aboutUs:
                intent = new Intent(this, AboutNudgeActivity.class);
                break;
            default:
                intent = new Intent(this, PrivacyPolicyActivity.class);
        }
        startActivity(intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns to the main activity
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    public static void setNudgeAdapter(NudgeArrayAdapter newValue) { nudgeAdapter = newValue; }
    /**
     * Class that fetches active nudges from database asynchronously
     */
    private class GetActiveNudgesTask extends AsyncTask<Context, Void, NudgeArrayAdapter>{

        @Override
        protected NudgeArrayAdapter doInBackground(Context... contexts) {
            nudgeAdapter = new NudgeArrayAdapter(contexts[0], NudgeDatabaseHelper.getPendingNudges());
            return nudgeAdapter;
        }

        protected void onPostExecute(NudgeArrayAdapter result){
            ((ListView) findViewById(R.id.activeNudgeList)).setAdapter(result);
        }
    }
}
