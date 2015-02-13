package com.ksp.nudge;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.ksp.database.NudgeMessagesDbHelper;


public class ActiveNudgesActivity extends ActionBarActivity {

    private static NudgeCursorAdapter nudgeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_nudges);
        this.setTitle(R.string.title_activity_active_nudges);
        buildListView();

        findViewById(R.id.newNudgeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
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

    private void buildListView() {
        NudgeMessagesDbHelper databaseHelper = new NudgeMessagesDbHelper(this);
        Cursor databaseCursor = databaseHelper.readMessagesFromDatabase();
        int[] adapterColumns = new int[]{R.id.nudgeRecipientText,R.id.nudgeMessageText,R.id.nudgeSendDateText};
        nudgeAdapter = new NudgeCursorAdapter(this,R.layout.active_nudge_item,
                databaseCursor, databaseCursor.getColumnNames(), adapterColumns,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        ListView activeNudgeList = (ListView) findViewById(R.id.activeNudgeList);
        activeNudgeList.setEmptyView(findViewById(R.id.instructionText));
        activeNudgeList.setAdapter(nudgeAdapter);
    }

    public static NudgeCursorAdapter getNudgeAdapter(){
        return nudgeAdapter;
    }
}
