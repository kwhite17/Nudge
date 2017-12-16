package com.ksp.nudge;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
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
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.message.NudgeInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static android.text.format.DateFormat.is24HourFormat;
import static android.util.Log.e;
import static android.widget.Toast.makeText;
import static com.ksp.message.MessageHandler.getNextSend;
import static com.ksp.nudge.R.array.frequency_array;
import static com.ksp.nudge.R.id.chooseContactText;
import static com.ksp.nudge.R.id.chooseDateButton;
import static com.ksp.nudge.R.id.chooseDateText;
import static com.ksp.nudge.R.id.chooseTimeText;
import static com.ksp.nudge.R.id.formActivityId;
import static com.ksp.nudge.R.id.frequencySeekBar;
import static com.ksp.nudge.R.id.frequencySeekBarLabel;
import static com.ksp.nudge.R.id.nudgeMessageTextField;
import static com.ksp.nudge.R.string.choose_frequency_instruction_text;
import static com.ksp.nudge.R.string.choose_frequency_instruction_title;
import static com.ksp.nudge.R.string.choose_recipient_instruction_text;
import static com.ksp.nudge.R.string.choose_recipient_instruction_title;
import static com.ksp.nudge.R.string.choose_send_datetime_instruction_text;
import static com.ksp.nudge.R.string.choose_send_datetime_instruction_title;
import static com.ksp.nudge.R.style.ShowcaseViewDark;
import static java.text.DateFormat.SHORT;
import static java.util.Arrays.asList;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class MessageFormActivity extends AppCompatActivity {
    private NudgeInfo nudge = new NudgeInfo();
    private SparseArray<int[]> showcaseViewData = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_form);
        initializeListeners();
//        initializeShowcaseViews();
        final RecipientEditTextView textView = findViewById(chooseContactText);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this);
        adapter.setShowMobileOnly(false);
        textView.setAdapter(adapter);
        String currentNudgeId = getIntent().getStringExtra("EDIT_NUDGE_ID");
        if (currentNudgeId != null) {
            NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(this);
            nudge = NudgeInfo.getMessageFromCursor(databaseHelper.getNudgeEntry(currentNudgeId));
            ((TextView)findViewById(chooseContactText)).setText(nudge.getRecipientInfo());
            ((EditText)findViewById(nudgeMessageTextField)).setText(nudge.getMessage());
            String[] frequencies = getResources().getStringArray(frequency_array);
            ((SeekBar)findViewById(frequencySeekBar)).
                    setProgress(asList(frequencies).indexOf(nudge.getFrequency()));
        }
        setDefaultTimeFromNudge();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_discard:
                toastAndChangeActivity("NudgeInfo discarded!", ActiveNudgesActivity.class);
                break;
            case R.id.action_save:
                try {
                    saveMessage();
                } catch (ParseException e) {
                    makeText(this, "Oops! Save failed!", Toast.LENGTH_SHORT).show();
                    e("Nudge Save Failure", e.getMessage());
                }
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

    private void initializeShowcaseViews() {
        int[] contactShowcaseData = new int[]{choose_recipient_instruction_title,
                choose_recipient_instruction_text, chooseDateButton};
        int[] dateShowcaseData = new int[]{choose_send_datetime_instruction_title,
                choose_send_datetime_instruction_text, frequencySeekBarLabel};
        int[] frequencyShowcaseData = new int[]{choose_frequency_instruction_title,
                choose_frequency_instruction_text, -1};
        showcaseViewData.put(chooseContactText, contactShowcaseData);
        showcaseViewData.put(chooseDateButton, dateShowcaseData);
        showcaseViewData.put(frequencySeekBarLabel, frequencyShowcaseData);
        displayShowcaseView(chooseContactText, showcaseViewData.get(chooseContactText));
    }

    private void displayShowcaseView(int target, final int[] showcaseData) {
        ShowcaseView showcaseView = new ShowcaseView.Builder(this,true)
                .setTarget(new ViewTarget(target, this))
                .setContentTitle(showcaseData[0])
                .setContentText(showcaseData[1])
                .singleShot(target)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        showcaseView.setVisibility(View.GONE);
                        if (showcaseData[2] != -1) {
                            displayShowcaseView(showcaseData[2],
                                    showcaseViewData.get(showcaseData[2]));
                        }
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {}
                })
                .build();
        showcaseView.setHideOnTouchOutside(true);
        showcaseView.hideButton();
        showcaseView.setStyle(ShowcaseViewDark);
    }

    private static String parseTimeFromCurrentSendTime(Date time) {
        return DateFormat.getTimeInstance(SHORT).format(time);
    }

    private static String parseDateFromCurrentSendTime(Date time) {
        return DateFormat.getDateInstance(SHORT).format(time);
    }

    private void setDefaultTimeFromNudge() {
        ((TextView) findViewById(chooseTimeText))
                .setText(parseTimeFromCurrentSendTime(nudge.getSendTime().getTime()));
        ((TextView) findViewById(chooseDateText))
                .setText(parseDateFromCurrentSendTime(nudge.getSendTime().getTime()));
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
            Calendar sendTime = currentActivity.nudge.getSendTime();
            sendTime.set(HOUR_OF_DAY, hourOfDay);
            sendTime.set(MINUTE, minute);
            String timeString = parseTimeFromCurrentSendTime(sendTime.getTime());
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
            Calendar sendTime = currentActivity.nudge.getSendTime();
            sendTime.set(Calendar.MONTH, monthOfYear);
            sendTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            sendTime.set(Calendar.YEAR, year);
            String dateString = parseDateFromCurrentSendTime(sendTime.getTime());
            ((TextView) currentActivity.findViewById(chooseDateText)).setText(dateString);
        }
    }

    /**
     * Save message into database and return to the ActiveNudges activity
     */
    private void saveMessage() throws ParseException {
        final RecipientEditTextView textView = findViewById(chooseContactText);
        DrawableRecipientChip[] chips = textView.getRecipients();
        String[] names = new String[chips.length];
        String[] numbers = new String[chips.length];
        for (int i = 0; i < chips.length; i++) {
            RecipientEntry entry = chips[i].getEntry();
            numbers[i] = entry.getDestination();
            names[i] = entry.getDisplayName() + " (" + ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), entry.getDestinationType(), "") + ")";
        }
        String numberString = "";
        for (int i = 0; i < numbers.length; i++) {
            numberString = numberString.concat(numbers[i]);
            if (i < numbers.length - 1) {
                numberString = numberString.concat(",");
            }
        }
        nudge.setRecipientNumber(numberString);
        if (names.length > 1) {
            nudge.setRecipientInfo(names[0] + " and " + Integer.toString(names.length - 1) + " others");
        } else {
            nudge.setRecipientInfo(names[0]);
        }
        if (nudge.isFilled()) {
            nudge.setSendTime(getNextSend(nudge.getSendTime(), nudge.getFrequency()));
            if (nudge.getId() == null){
                Log.i("Saving New Nudge",
                        new NudgeDatabaseHelper(this).writeMessageToDatabase(nudge));
            } else{
                Log.i("Updating Current Nudge",
                        new NudgeDatabaseHelper(this).updateExistingMessage(nudge));
            }
            SendMessageService.setServiceAlarm(this, nudge.getSendTimeAsString());
            toastAndChangeActivity("NudgeInfo saved!", ActiveNudgesActivity.class);
        } else {
            makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
        }
    }
}
