package com.ksp.nudge;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ksp.database.NudgeDatabaseHelper;


public class ActiveNudgesActivity extends ActionBarActivity {

    private static NudgeCursorAdapter nudgeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_nudges);

        //build the list of active nudges
        ((ListView) findViewById(R.id.activeNudgeList)).setEmptyView(findViewById(R.id.instructionText));
        new GetActiveNudgesTask().execute(this);

        //setup ShowcaseView for first time instructions
        final ShowcaseView nudgeButtonShowcase = new ShowcaseView.Builder(this,true).setTarget(new ViewTarget(R.id.newNudgeButton,this))
                .setContentTitle(R.string.title_activity_message_form)
                .setContentText(R.string.new_nudge_instruction_text)
                .singleShot(R.id.newNudgeButton)
                .hideOnTouchOutside()
                .build();
        nudgeButtonShowcase.hideButton();
        nudgeButtonShowcase.setStyle(R.style.ShowcaseViewDark);
        findViewById(R.id.newNudgeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nudgeButtonShowcase.hide();
                changeActivity(MessageFormActivity.class);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_active_nudges, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        changeActivity(this.getClass());
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    /**
     *
     * @param activityClass, the activity to be changed to
     */
    private void changeActivity(Class<?> activityClass){
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }

    public static NudgeCursorAdapter getNudgeAdapter(){
        return nudgeAdapter;
    }

    /**
     * Class that fetches active nudges from database asynchronously
     */
    private class GetActiveNudgesTask extends AsyncTask<Context,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Context... contexts) {
            NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(contexts[0]);
            return databaseHelper.readMessagesFromDatabase();
        }

        protected void onPostExecute(Cursor result){
            int[] adapterColumns = new int[]{R.id.nudgeRecipientText,
                    R.id.nudgeMessageText,R.id.nudgeSendDateText};
            ActiveNudgesActivity.nudgeAdapter = new NudgeCursorAdapter(getApplicationContext(),
                    R.layout.active_nudge_item, result, result.getColumnNames(),
                    adapterColumns, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            ((ListView)findViewById(R.id.activeNudgeList)).setAdapter(ActiveNudgesActivity.nudgeAdapter);
        }
    }
}
