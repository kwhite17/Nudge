package com.ksp.nudge;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageFormActivity extends Activity {

    private static final int REQUEST_CONTACTS = 1;
    private String contactNumber = "";
    protected Calendar currentSendDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_form);

        Button selectContactButton = (Button) findViewById(R.id.selectContactBtn);
        selectContactButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                getContact();
            }

        });

        Button chooseTimeButton = (Button) findViewById(R.id.chooseTimeButton);
        String timeString = SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT)
                .format(this.currentSendDate.getTime());
        chooseTimeButton.setText("Edit Initial Send Time: " + timeString);

        Button chooseDateButton = (Button) findViewById(R.id.chooseDateButton);
        chooseDateButton.setText("Edit Current Send Date: " +
                (this.currentSendDate.get(Calendar.MONTH)+1) + "/" +
                this.currentSendDate.get(Calendar.DAY_OF_MONTH) + "/" +
                this.currentSendDate.get(Calendar.YEAR));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_form, menu);
        return true;
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
                        MessageHandler.getNextSend(this.currentSendDate, frequency, this),
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

    /**
     *
     * @param activityClass, the activity to be changed to
     */
    private void changeActivity(Class<?> activityClass){
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener{

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
         * 
         * Sets the Time to the current time
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

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
            currentActivity.currentSendDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            currentActivity.currentSendDate.set(Calendar.MINUTE, minute);
            String timeString = SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT)
                    .format(currentActivity.currentSendDate.getTime());
            Button chooseTimeButton = (Button) currentActivity.findViewById(R.id.chooseTimeButton);
            chooseTimeButton.setText("Edit Current Send Time: " + timeString);
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
         * 
         * Sets the date to the current date
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
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
            currentActivity.currentSendDate.set(Calendar.MONTH, monthOfYear);
            currentActivity.currentSendDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            currentActivity.currentSendDate.set(Calendar.YEAR, year);

            Button chooseDateButton = (Button) currentActivity.findViewById(R.id.chooseDateButton);
            chooseDateButton.setText("Edit Current Send Date: " + (monthOfYear+1)
                    + "/" + dayOfMonth + "/" + year);
        }
    }

}
