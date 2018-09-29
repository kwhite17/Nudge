package com.ksp.nudge;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.message.NudgeInfo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.text.format.DateFormat.is24HourFormat;
import static android.util.Log.e;
import static android.widget.Toast.makeText;
import static com.ksp.message.MessageHandler.getNextSend;
import static com.ksp.nudge.R.array.frequency_array;
import static com.ksp.nudge.R.id.chooseContactText;
import static com.ksp.nudge.R.id.chooseDateText;
import static com.ksp.nudge.R.id.chooseTimeText;
import static com.ksp.nudge.R.id.formActivityId;
import static com.ksp.nudge.R.id.frequencySeekBar;
import static com.ksp.nudge.R.id.frequencySeekBarLabel;
import static com.ksp.nudge.R.id.nudgeMessageTextField;
import static java.text.DateFormat.SHORT;
import static java.util.Arrays.asList;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class MessageFormActivity extends AppCompatActivity {
    private static final int REQUEST_NUDGE_CREATION = 1;
    private NudgeInfo nudge = new NudgeInfo();
    private Instant tempInstant = Instant.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_form);
        initializeListeners();
        final RecipientEditTextView textView = findViewById(chooseContactText);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this);
        adapter.setShowMobileOnly(false);
        textView.setAdapter(adapter);
        String currentNudgeId = getIntent().getStringExtra("EDIT_NUDGE_ID");
        if (currentNudgeId != null) {
            NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(this);
            nudge = NudgeInfo.getMessageFromCursor(databaseHelper.getNudgeEntry(currentNudgeId));
            ((EditText)findViewById(nudgeMessageTextField)).setText(nudge.getMessage());
            String[] frequencies = getResources().getStringArray(frequency_array);
            ((SeekBar)findViewById(frequencySeekBar)).
                    setProgress(asList(frequencies).indexOf(nudge.getFrequency()));
            textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (textView.getWidth() > 0 && textView.getRecipients().length == 0) {
                        String[] numbers = nudge.getRecipientNumber().split(",");
                        String[] names = nudge.getRecipientInfo().split(",");
                        for (int i = 0; i < names.length; i++) {
                            textView.submitItem(names[i], numbers[i]);
                        }
                    }
                }
            });
        }
        setDefaultTimeFromNudge();
    }

    @Override
    protected void onResume(){
        super.onResume();
        validatePermissions();
    }

    private void validatePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS},
                    REQUEST_NUDGE_CREATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_NUDGE_CREATION:
                if (grantResults.length <= 0) {
                    toastAndChangeActivity("Can't create Nudge without permissions",
                            ActiveNudgesActivity.class);
                } else {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            toastAndChangeActivity("Can't create Nudge without permissions",
                                    ActiveNudgesActivity.class);
                            break;
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_discard:
                toastAndChangeActivity("NudgeInfo discarded!", ActiveNudgesActivity.class);
                break;
            case R.id.action_save:
                saveMessage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns to the main activity
     */
    @Override
    public void onBackPressed() {
        toastAndChangeActivity("NudgeInfo discarded!", ActiveNudgesActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_form, menu);
        return true;
    }

    private void initializeListeners() {
        final ScrollView scrollView = findViewById(formActivityId);
        scrollView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        scrollView.smoothScrollTo(0,
                                (int) findViewById(frequencySeekBarLabel).getY());
                    }
                });
        ((SeekBar) findViewById(frequencySeekBar))
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView progressBarLabel = findViewById(frequencySeekBarLabel);
                        String[] frequencies = getResources().getStringArray(frequency_array);
                        nudge.setFrequency(frequencies[progress]);
                        progressBarLabel.setText(frequencies[progress]);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        ((EditText) findViewById(nudgeMessageTextField)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                nudge.setMessage(s.toString());
            }
        });
    }
    private static String parseTimeFromCurrentSendTime(long time) {
        return DateFormat.getTimeInstance(SHORT, Locale.getDefault()).format(new Date(time));
    }

    private static String parseDateFromCurrentSendTime(long time) {
        return DateFormat.getDateInstance(SHORT, Locale.getDefault()).format(new Date(time));
    }

    private void setDefaultTimeFromNudge() {
        ((TextView) findViewById(chooseTimeText))
                .setText(parseTimeFromCurrentSendTime(DateTimeZone.getDefault()
                        .convertUTCToLocal(nudge.getSendTime().getMillis())));
        ((TextView) findViewById(chooseDateText))
                .setText(parseDateFromCurrentSendTime(DateTimeZone.getDefault()
                        .convertUTCToLocal(nudge.getSendTime().getMillis())));
    }

    /**
     * @param activityClass, the activity to be changed to
     * @param toastMessage, the temporary message presented to the user
     */
    private void toastAndChangeActivity(String toastMessage, Class<?> activityClass) {
        makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
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
            implements TimePickerDialog.OnTimeSetListener {

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
         * 
         * Sets the Time to the current time
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(HOUR_OF_DAY);
            int minute = c.get(MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    is24HourFormat(getActivity()));
        }

        /*
         * (non-Javadoc)
         * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker,
         * int, int)
         * 
         * Changes the edit current message send time. Also displays the current
         *  message send time on the edit current send time button. 
         */
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            MessageFormActivity currentActivity = (MessageFormActivity) getActivity();
            currentActivity.tempInstant = Instant.ofEpochMilli(currentActivity.tempInstant.toDateTime()
                    .withHourOfDay(hourOfDay).withMinuteOfHour(minute).getMillis());
            String timeString = parseTimeFromCurrentSendTime(currentActivity.tempInstant.getMillis());
            ((TextView) currentActivity.findViewById(chooseTimeText)).setText(timeString);
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
         * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker,
          * int, int, int)
         * 
         * Changes the edit current message send date. Also displays the current
         *  message send time on the edit current send date button. 
         */
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            MessageFormActivity currentActivity = (MessageFormActivity) getActivity();
            DateTime dateTime = new DateTime(year, monthOfYear + 1, dayOfMonth, 0 ,0);
            currentActivity.tempInstant = Instant.ofEpochMilli(dateTime.getMillis());
            String dateString = parseDateFromCurrentSendTime(currentActivity.tempInstant.getMillis());
            ((TextView) currentActivity.findViewById(chooseDateText)).setText(dateString);
        }
    }

    /**
     * Save message into database and return to the ActiveNudges activity
     */
    private void saveMessage() {
        final RecipientEditTextView textView = findViewById(chooseContactText);
        DrawableRecipientChip[] chips = textView.getRecipients();
        String[] names = new String[chips.length];
        String[] numbers = new String[chips.length];
        for (int i = 0; i < chips.length; i++) {
            RecipientEntry entry = chips[i].getEntry();
            numbers[i] = entry.getDestination();
            names[i] = entry.getDisplayName();
        }
        String numberString = "";
        for (int i = 0; i < numbers.length; i++) {
            numberString = numberString.concat(numbers[i]);
            if (i < numbers.length - 1) {
                numberString = numberString.concat(",");
            }
        }
        String namesString = "";
        for (int i = 0; i < names.length; i++) {
            namesString = namesString.concat(names[i]);
            if (i < numbers.length - 1) {
                namesString = namesString.concat(",");
            }
        }
        nudge.setRecipientNumber(numberString);
        nudge.setRecipientInfo(namesString);
        if (nudge.isFilled()) {
            nudge.setSendTime(getNextSend(tempInstant, nudge.getFrequency()).getMillis());
            if (nudge.getId() == null){
                Log.i("Saving New Nudge",
                        new NudgeDatabaseHelper(this).writeMessageToDatabase(nudge));
            } else{
                Log.i("Updating Current Nudge",
                        new NudgeDatabaseHelper(this).updateExistingMessage(nudge));
            }
            SendMessageWorker.scheduleMessage(nudge.getId(), nudge.getSendTime().getMillis());
            toastAndChangeActivity("NudgeInfo saved!", ActiveNudgesActivity.class);
        } else {
            makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
        }
    }
}
