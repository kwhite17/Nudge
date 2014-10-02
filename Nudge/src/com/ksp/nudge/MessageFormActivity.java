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

    private static final int REQUEST_CONTACTPICKER = 2113;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button contactBtn = (Button) findViewById(R.id.selectContactBtn);
        contactBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                getContact();
            }

        });
    }

    protected void changeToActiveReminders(boolean fromMenu) {
        try{
            if (!fromMenu){

                EditText phoneText = (EditText) findViewById(R.id.contactNameView);
                String phoneNumber = phoneText.getEditableText().toString();
                EditText msgText = (EditText) findViewById(R.id.msgText);
                String msg = msgText.getEditableText().toString();
                RadioGroup freqGroup = (RadioGroup) findViewById(R.id.FrequencyGroup);
                String frequency =((RadioButton) findViewById(freqGroup.getCheckedRadioButtonId())).getText().toString();
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);

                System.out.println(writeMsgToDb(phoneNumber,
                        MessageHandler.getNextSend(timePicker.getCurrentHour(), timePicker.getCurrentMinute(), frequency),
                        msg,
                        frequency));

            }
            Toast.makeText(this, "Message saved!", Toast.LENGTH_SHORT).show();

            changeActivity(ActiveRemindersActivity.class);
        }
        catch(Exception e){
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message_form, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
        case R.id.action_settings:
            return true;
        case R.id.action_discard:
            this.changeToActiveReminders(true);
            break;
        case R.id.action_save:
            this.changeToActiveReminders(false);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        changeActivity(ActiveRemindersActivity.class);
    }

    public void getContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, 
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACTPICKER);
    }

    // Listen for results.
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        //		super.onActivityResult(requestCode, resultCode, data);
        try{
            Uri result = data.getData();
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
        } catch(RuntimeException e){
            Log.i("ContactSelection", "User backed out of contacts.");
        }
    }

    private void changeActivity(Class<?> activityClass){
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }
}
