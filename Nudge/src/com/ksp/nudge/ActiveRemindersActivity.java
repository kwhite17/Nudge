package com.ksp.nudge;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import com.ksp.database.MessageHandler;
import com.ksp.database.MessageReaderContract.MessageEntry;
import com.ksp.database.MessageReaderDbHelper;

public class ActiveRemindersActivity extends Activity {
    MessageReaderDbHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_reminders);

        this.setTitle(R.string.title_activity_scheduled_messages);
        Intent serviceIntent = new Intent(this,SendMessageService.class);
        this.startService(serviceIntent);

        databaseHelper = new MessageReaderDbHelper(this);

        SparseArray<String> activeMessages = readMsgsfromDb();
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
    
    //TODO: move to db helper
    private SparseArray<String> readMsgsfromDb() {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        SparseArray<String> msgMap = new SparseArray<String>();
        String[] projection = {
                MessageEntry._ID,
                MessageEntry.COLUMN_NAME_RECIPIENT,
                MessageEntry.COLUMN_NAME_SEND_TIME,
                MessageEntry.COLUMN_NAME_MESSAGE,
        };
        String sortOrder = MessageEntry.COLUMN_NAME_RECIPIENT + " DESC";
        Cursor msgCursor = database.query(MessageEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        msgCursor.moveToFirst();
        while (!msgCursor.isAfterLast()){
            String recipient = msgCursor.getString(msgCursor.getColumnIndex(MessageEntry.COLUMN_NAME_RECIPIENT));
            String message = msgCursor.getString(msgCursor.getColumnIndex(MessageEntry.COLUMN_NAME_MESSAGE));
            String sendDate = msgCursor.getString(msgCursor.getColumnIndex(MessageEntry.COLUMN_NAME_SEND_TIME));

            msgMap.put(msgCursor.getInt(msgCursor.getColumnIndex(MessageEntry._ID)), MessageHandler.formatMessage(recipient, message, sendDate));

            msgCursor.moveToNext();
        }
        msgCursor.close();
        return msgMap;
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
            deleteBtn.setBackgroundResource(R.drawable.ic_action_discard);
            deleteBtn.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    MessageHandler.deleteMessage(Integer.toString(messageId), databaseHelper);
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
            System.out.println("Failure to add message...");
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
