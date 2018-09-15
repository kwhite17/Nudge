package com.ksp.nudge;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.ksp.database.NudgeDatabaseHelper;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static com.ksp.nudge.R.id.activeNudgeList;
import static com.ksp.nudge.R.id.newNudgeButton;
import static com.ksp.nudge.R.id.nudgeMessageText;
import static com.ksp.nudge.R.id.nudgeRecipientText;
import static com.ksp.nudge.R.id.nudgeSendDateText;
import static com.ksp.nudge.R.layout.active_nudge_item;
import static com.ksp.nudge.R.menu.menu_active_nudges;


public class ActiveNudgesActivity extends AppCompatActivity {

    private static NudgeCursorAdapter nudgeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_nudges);

        //setup ShowcaseView for first time instructions
//        final ShowcaseView nudgeButtonShowcase = initializeShowcaseView();
        findViewById(newNudgeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                nudgeButtonShowcase.hide();
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
        ((ListView) findViewById(activeNudgeList)).setEmptyView(findViewById(R.id.instructionText));
        if (nudgeAdapter == null) {
            Log.i("NUDGE_ADAPTER", "Adapter failed to load on startup!");
            new GetActiveNudgesTask().execute(this);
        } else {
            Log.i("NUDGE_ADAPTER", "Adapter loaded on startup!");
            nudgeAdapter.refreshAdapter(new NudgeDatabaseHelper(this));
            ((ListView)findViewById(activeNudgeList)).setAdapter(nudgeAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(menu_active_nudges, menu);
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

    public static NudgeCursorAdapter getNudgeAdapter(){
        return nudgeAdapter;
    }
    public static void setNudgeAdapter(NudgeCursorAdapter newValue) { nudgeAdapter = newValue; }
    /**
     * Class that fetches active nudges from database asynchronously
     */
    private class GetActiveNudgesTask extends AsyncTask<Context,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Context... contexts) {
            return new NudgeDatabaseHelper(contexts[0]).readMessagesFromDatabase();
        }

        protected void onPostExecute(Cursor result){
            int[] adapterColumns = new int[]{nudgeRecipientText,
                    nudgeMessageText, nudgeSendDateText};
            ActiveNudgesActivity.nudgeAdapter = new NudgeCursorAdapter(ActiveNudgesActivity.this,
                    active_nudge_item, result, result.getColumnNames(),
                    adapterColumns, FLAG_REGISTER_CONTENT_OBSERVER);
            ((ListView)findViewById(activeNudgeList)).setAdapter(ActiveNudgesActivity.nudgeAdapter);
        }
    }
}
