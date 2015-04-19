package com.ksp.nudge;

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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ksp.message.Message;
import com.ksp.message.MessageHandler;
import com.ksp.database.NudgeDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageFormActivity extends ActionBarActivity {
    private static final int REQUEST_CONTACTS = 1;
    private Message nudge = new Message();
    protected Calendar currentSendDate = Calendar.getInstance();
    private SparseArray<int[]> showcaseViewData = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_form);

        CardView chooseContactButton = (CardView) findViewById(R.id.chooseContactButton);
        TextView chooseTimeText = (TextView) findViewById(R.id.chooseTimeText);
        TextView chooseDateText = (TextView) findViewById(R.id.chooseDateText);
        String timeString = SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT)
                .format(this.currentSendDate.getTime());
        chooseContactButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getContact();
            }

        });
        chooseTimeText.setText("Select a Time: " + timeString);
        chooseDateText.setText("Select a Date: " +
                (this.currentSendDate.get(Calendar.MONTH) + 1) + "/" +
                this.currentSendDate.get(Calendar.DAY_OF_MONTH) + "/" +
                this.currentSendDate.get(Calendar.YEAR));
        initializeListeners();
        initializeShowcaseViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_discard:
                Toast.makeText(this, "Message discarded!", Toast.LENGTH_SHORT).show();
                changeActivity(ActiveNudgesActivity.class);
                break;
            case R.id.action_save:
                this.saveMessage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns to the main activity
     */
    @Override
    public void onBackPressed(){
        changeActivity(ActiveNudgesActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_form, menu);
        return true;
    }

    private void initializeListeners() {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.formActivityId);
        scrollView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.i("New Layout Height: ", Integer.toString(scrollView.getHeight()));
                        scrollView.smoothScrollTo(0, (int) findViewById(R.id.frequencyBarLabel).getY());
                    }
                });
        ((SeekBar) findViewById(R.id.frequencyBar))
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView progressBarLabel = (TextView) findViewById(R.id.frequencyBarLabel);
                        String[] frequencies = getResources().getStringArray(R.array.frequencyArray);
                        nudge.setFrequency(frequencies[progress]);
                        progressBarLabel.setText(frequencies[progress]);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
        ((EditText) findViewById(R.id.messageTextField)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                nudge.setMessage(s.toString());
            }
        });
    }

    private void initializeShowcaseViews() {
        int[] contactShowcaseData = new int[]{R.string.choose_recipient_instruction_title,
                R.string.choose_recipient_instruction_text,R.id.chooseDateButton};
        int[] dateShowcaseData = new int[]{R.string.choose_send_datetime_instruction_title,
                R.string.choose_send_datetime_instruction_text,-1};
        showcaseViewData.put(R.id.chooseContactButton, contactShowcaseData);
        showcaseViewData.put(R.id.chooseDateButton, dateShowcaseData);
        displayShowcaseView(R.id.chooseContactButton, showcaseViewData.get(R.id.chooseContactButton));
    }

    private void displayShowcaseView(int target, final int[] showcaseData){
        ShowcaseView contactShowcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(target, this))
                .setContentTitle(showcaseData[0])
                .setContentText(showcaseData[1])
                .singleShot(target)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {}
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        showcaseView.setVisibility(View.GONE);
                        if (showcaseData[2] != -1) {
                            displayShowcaseView(showcaseData[2], showcaseViewData.get(showcaseData[2]));
                        }
                    }
                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {}
                })
                .build();
        contactShowcaseView.setHideOnTouchOutside(true);
        contactShowcaseView.hideButton();
        contactShowcaseView.setStyle(R.style.ShowcaseViewDark);
    }

    /**
     * Save message into database and return to the ActiveNudges activity
     */
    protected void saveMessage() {
        if (nudge.isFilled() && currentSendDate != null) {
            Log.i("Saving Message", new NudgeDatabaseHelper(this).
                    writeMessageToDatabase(nudge, MessageHandler.getNextSend(this.currentSendDate,
                            nudge.getFrequency(), this)));
            Toast.makeText(this, "Message saved!", Toast.LENGTH_SHORT).show();
            changeActivity(ActiveNudgesActivity.class);
        } else{
            Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
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
                String contactNumberType = " (".concat((String) Phone.getTypeLabel(this.getResources(),cursor
                        .getInt(cursor.getColumnIndex(Phone.TYPE)),"")).concat( ")");
                String contactName = cursor.getString(contactNameIndex);
                nudge.setRecipientInfo(contactName.concat(contactNumberType));
                nudge.setRecipientNumber(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
                ((TextView) this.findViewById(R.id.chooseContactText)).
                        setText("Change Contact: ".concat(nudge.getRecipientInfo()));

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
            TextView chooseTimeText = (TextView) currentActivity.findViewById(R.id.chooseTimeText);
            chooseTimeText.setText("Edit Current Send Time: " + timeString);
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
            TextView chooseDateText = (TextView) currentActivity.findViewById(R.id.chooseDateText);
            chooseDateText.setText("Edit Current Send Date: " + (monthOfYear + 1)
                    + "/" + dayOfMonth + "/" + year);
        }
    }

}
