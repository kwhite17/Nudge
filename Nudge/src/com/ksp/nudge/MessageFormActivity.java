package com.ksp.nudge;

import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ksp.database.MessageHandler;
import com.ksp.database.NudgeMessagesDbHelper;

public class MessageFormActivity extends Activity { 

    private static final int REQUEST_CONTACTS = 1;
    private final String currentSendDateKey = "currentSendDate";
    private final String currentSendMonthKey = "currentSendMonth";
    private final String currentSendYearKey = "currentSendYear";
    private final String currentSendHourKey = "currentSendHour";
    private final String currentDisplayHourKey = "currentDisplayHour";
    private final String currentSendMinuteKey = "currentSendMinute";
    private String contactNumber = "";
    protected HashMap<String,Integer> dateTimeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_form);

        initializeDateTimeMap();
        String period = this.dateTimeMap.get(this.currentSendHourKey) >= 12? "PM":"AM";
        
        Button selectContactButton = (Button) findViewById(R.id.selectContactBtn);
        selectContactButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                getContact();
            }

        });

        Button chooseTimeButton = (Button) findViewById(R.id.chooseTimeButton);
        chooseTimeButton.setText("Edit Current Send Time: " + 
                this.dateTimeMap.get(this.currentDisplayHourKey) +":" + 
                this.dateTimeMap.get(this.currentSendMinuteKey) + " " + period);
        
        Button chooseDateButton = (Button) findViewById(R.id.chooseDateButton);
        chooseDateButton.setText("Edit Current Send Date: " + 
                (this.dateTimeMap.get(this.currentSendMonthKey) + 1) + "/" + 
                this.dateTimeMap.get(this.currentSendDateKey) + "/" + 
                this.dateTimeMap.get(this.currentSendYearKey));
    }
    
    /**
     * Initializes the date time map that stores the results of user inputs
     * for date/time values
     */
    private void initializeDateTimeMap() {
        this.dateTimeMap = new HashMap<String,Integer>();
        Calendar currentCalendar = Calendar.getInstance();
        dateTimeMap.put(this.currentSendMonthKey, currentCalendar.get(Calendar.MONTH));
        dateTimeMap.put(this.currentSendDateKey, currentCalendar.get(Calendar.DATE));
        dateTimeMap.put(this.currentSendYearKey, currentCalendar.get(Calendar.YEAR));
        dateTimeMap.put(this.currentSendHourKey, currentCalendar.get(Calendar.HOUR_OF_DAY));
        dateTimeMap.put(this.currentSendMinuteKey, currentCalendar.get(Calendar.MINUTE));
        
        int displayHour = dateTimeMap.get(this.currentSendHourKey);
        if (displayHour == 0){
            displayHour = 12;
        } else if (displayHour > 12){
            displayHour -= 12;
        }
        dateTimeMap.put(this.currentDisplayHourKey, displayHour);
    }

    /**
     * Changes to Active Reminders activity. May save message form data
     * depending on how the method was triggered
     * @param saveMessage, if the data in the message for should be saved
     */
    protected void changeToActiveReminders(boolean saveMessage) {
        try{
            if (saveMessage){

                TextView phoneText = (TextView) findViewById(R.id.contactNameView);
                String contactName = phoneText.getText().toString();
                if (contactName.contains("[")){
                    throw new Exception();
                }
                EditText msgText = (EditText) findViewById(R.id.msgText);
                String msg = msgText.getEditableText().toString();
                RadioGroup freqGroup = (RadioGroup) findViewById(R.id.FrequencyGroup);
                String frequency =((RadioButton) findViewById(freqGroup.getCheckedRadioButtonId())).getText().toString();

                Log.i("Saving Message", new NudgeMessagesDbHelper(this).writeMsgToDb(contactName,
                        contactNumber,
                        msg,
                        MessageHandler.getNextSend(this.dateTimeMap.get(this.currentSendHourKey), 
                                this.dateTimeMap.get(this.currentSendMinuteKey), frequency), 
                                frequency));
                
                Toast.makeText(this, "Message saved!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Message deleted!", Toast.LENGTH_SHORT).show();
            }
            changeActivity(ActiveRemindersActivity.class);
        }
        catch(Exception e){
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
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

            int contactNameIndex= cursor.getColumnIndex(Phone.DISPLAY_NAME);
            if (cursor.moveToFirst())
            {
                contactNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                ((TextView) this.findViewById(R.id.contactNameView)).setText(cursor.getString(contactNameIndex));

            }
            cursor.close();
        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }
    
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
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

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
        
        /*
         * (non-Javadoc)
         * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker, int, int)
         * 
         * Changes the edit current message send time. Also displays the current
         *  message send time on the edit current send time button. 
         */
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            MessageFormActivity currentActivity = (MessageFormActivity)this.getActivity();
            currentActivity.dateTimeMap.put(currentActivity.currentSendHourKey,hourOfDay);
            currentActivity.dateTimeMap.put(currentActivity.currentSendMinuteKey,minute);
            String period = hourOfDay >= 12? "PM":"AM";
            
            int displayHour = hourOfDay;
            if (hourOfDay == 0){
                displayHour = 12;
            } else if (hourOfDay > 12){
                displayHour -= 12;
            }
            currentActivity.dateTimeMap.put(currentActivity.currentDisplayHourKey, displayHour);
            
            Button chooseTimeButton = (Button) currentActivity.findViewById(R.id.chooseTimeButton);
            chooseTimeButton.setText("Edit Current Send Time: " + 
                    currentActivity.dateTimeMap.get(currentActivity.currentDisplayHourKey)
                    + ":" + currentActivity.dateTimeMap.get(currentActivity.currentSendMinuteKey) 
                    + " " + period);
        }
    }

    public static class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
        
        /*
         * (non-Javadoc)
         * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
         * 
         * Changes the edit current message send date. Also displays the current
         *  message send time on the edit current send date button. 
         */
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            MessageFormActivity currentActivity = (MessageFormActivity)this.getActivity();
            currentActivity.dateTimeMap.put(currentActivity.currentSendMonthKey,monthOfYear);
            currentActivity.dateTimeMap.put(currentActivity.currentSendDateKey,dayOfMonth);
            currentActivity.dateTimeMap.put(currentActivity.currentSendYearKey,year);
            
            Button chooseDateButton = (Button) currentActivity.findViewById(R.id.chooseDateButton);
            chooseDateButton.setText("Edit Current Send Date: " + (monthOfYear+1)
                    + "/" + dayOfMonth + "/" + year);
        }
    }

}
