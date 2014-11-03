package com.ksp.nudge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksp.database.NudgeMessagesDbHelper;

public class ActiveRemindersActivity extends Activity {
    NudgeMessagesDbHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_reminders);

        this.setTitle(R.string.title_activity_scheduled_messages);
        Intent serviceIntent = new Intent(this,SendMessageService.class);
        this.startService(serviceIntent);

        SparseArray<String> activeMessages = new NudgeMessagesDbHelper(this).readMsgsfromDb();
        if (activeMessages.size() == 0){
            displayInstructionMsg();
        }
        else{
            int key = 0;
            for(int i = 0; i < activeMessages.size(); i++) {
                key = activeMessages.keyAt(i);
                this.addMessage(activeMessages.get(key),key);
            }
        }

    }
    
    /**
     * Displays the instruction message when no messages are scheduled
     */
    private void displayInstructionMsg() {
        LinearLayout reminderLayout = (LinearLayout) findViewById(R.id.ActiveRemindersLayout);
        RelativeLayout instructionLayout = new RelativeLayout(this);
        TextView instructionTxtView = new TextView(this);
        LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        instructionLayout.setGravity(Gravity.CENTER);
        instructionLayout.setLayoutParams(lp);
        instructionLayout.setId(R.id.introLayoutId);
        instructionTxtView.setText(R.string.instruction_message);

        reminderLayout.addView(instructionLayout);
        instructionLayout.addView(instructionTxtView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_reminders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.new_reminder){
            changeActivity(MessageFormActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        changeActivity(this.getClass());;
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
    
    /**
     * Adds message to the active reminders screen
     * @param message, the message to be added to the screen
     * @param btnId, the id of the message in the database
     */
    public void addMessage(String message, final int messageId) {
        LinearLayout reminderLayout = (LinearLayout) this.findViewById(R.id.ActiveRemindersLayout);
        LinearLayout itemLayout = new LinearLayout(this);
        LinearLayout messageLayout = new LinearLayout(this);
        LinearLayout buttonLayout = new LinearLayout(this);

        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));		

        if (reminderLayout != null) {
            TextView messageText = new TextView(this);
            //			Button editBtn = new Button(this);
            ImageButton deleteBtn = new ImageButton(this);

            messageText.setText(message);
            deleteBtn.setBackgroundResource(R.drawable.ic_action_discard_dark);
            deleteBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    new NudgeMessagesDbHelper(ActiveRemindersActivity.this).deleteMessage(Integer.toString(messageId));
                    changeActivity(ActiveRemindersActivity.class);
                }

            });
            deleteBtn.setTag("delBtn" + Integer.toString(messageId));
            //			editBtn.setText("Edit");
            //			editBtn.setSingleLine(true);

            messageLayout.addView(messageText);
            //			buttonLayout.addView(editBtn);
            buttonLayout.addView(deleteBtn);
            itemLayout.addView(messageLayout);
            itemLayout.addView(buttonLayout);
            reminderLayout.addView(itemLayout);
        } else {
            Log.w("Display Message Failed","Failure to add message...");
        }
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
}
