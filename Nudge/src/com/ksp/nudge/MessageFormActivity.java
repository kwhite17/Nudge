package com.ksp.nudge;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ksp.database.MessageHandler;
import com.ksp.database.MessageReaderContract.MessageEntry;
import com.ksp.database.MessageReaderDbHelper;

public class MessageFormActivity extends Activity { 

    private static final int REQUEST_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button selectContactBtn = (Button) findViewById(R.id.selectContactBtn);
        selectContactBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                getContact();
            }

        });
    }
    
    /**
     * Changes to Active Reminders activity. May save message form data
     * depending on how the method was triggered
     * @param saveMessage, if the data in the message for should be saved
     */
    protected void changeToActiveReminders(boolean saveMessage) {
        try{
            if (saveMessage){

                EditText phoneText = (EditText) findViewById(R.id.contactNameView);
                String phoneNumber = phoneText.getEditableText().toString();
                EditText msgText = (EditText) findViewById(R.id.msgText);
                String msg = msgText.getEditableText().toString();
                RadioGroup freqGroup = (RadioGroup) findViewById(R.id.FrequencyGroup);
                String frequency =((RadioButton) findViewById(freqGroup.getCheckedRadioButtonId())).getText().toString();
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);

                Log.i("Saving Message", writeMsgToDb(phoneNumber,
                        MessageHandler.getNextSend(timePicker.getCurrentHour(), timePicker.getCurrentMinute(), frequency),
                        msg,
                        frequency));

            }
            Toast.makeText(this, "Message saved!", Toast.LENGTH_SHORT).show();

            changeActivity(ActiveRemindersActivity.class);
        }
        catch(Exception e){
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            Log.e("Save Message Failed", e.getMessage());
        }
    }
    
    //TODO: move to dbhelper
    private String writeMsgToDb(String number, String time, String msg,
            String frequency) {

        MessageReaderDbHelper dbHelper = new MessageReaderDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues rowValues = new ContentValues();

        rowValues.put(MessageEntry.COLUMN_NAME_RECIPIENT, number);
        rowValues.put(MessageEntry.COLUMN_NAME_SEND_TIME, time);
        rowValues.put(MessageEntry.COLUMN_NAME_MESSAGE, msg);
        rowValues.put(MessageEntry.COLUMN_NAME_FREQUENCY, frequency);

        long insertId = db.insert(MessageEntry.TABLE_NAME, null, rowValues);
        return (insertId == -1) ? "Insertion failed": "Insertion successful";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_form, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
        case R.id.action_settings:
            return true;
        case R.id.action_discard:
            this.changeToActiveReminders(false);
            break;
        case R.id.action_save:
            this.changeToActiveReminders(true);
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Returns to the main activity
     */
    @Override
    public void onBackPressed(){
        changeActivity(ActiveRemindersActivity.class);
    }
    
    /**
     * Starts an activity that allows the user to select a contact to send a 
     * message to
     */
    public void getContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, 
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACTS);
    }
    
    /*
     * Handles information about the contact selected as a result of the 
     * getContact() method
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        
        Uri result = null;
        try{
            result = data.getData();
        } catch(RuntimeException e){
            Log.i("ContactSelection", "User backed out of contacts.");
        }
        
        if (result != null){
            String id = result.getLastPathSegment();
            Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, null,
                    Phone._ID + " = ?",
                    new String[] {id}, null);
            int phoneIdx = cursor.getColumnIndex(Phone.NUMBER);
            if (cursor.moveToFirst())
            {

                ((EditText) this.findViewById(R.id.contactNameView)).setText(cursor.getString(phoneIdx));

            }
            cursor.close();
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
