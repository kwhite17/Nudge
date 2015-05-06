package com.ksp.message;

import android.database.Cursor;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import static android.provider.BaseColumns._ID;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_FREQUENCY;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME;

/**
 * The Message class is data structure responsible for maintaining all the information a user
 * provides about the Nudge they are attempting to create.
 */
public class Message {
    private int id = -1;
    private String recipientInfo;
    private String recipientNumber;
    private Calendar sendTime = Calendar.getInstance();
    private String message = "";
    private String frequency = "Weekly";

    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }

    public String getRecipientInfo() {
        return recipientInfo;
    }
    public void setRecipientInfo(String recipientInfo) {
        this.recipientInfo = recipientInfo;
    }

    public String getRecipientNumber() { return recipientNumber; }
    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }

    public Calendar getSendTime() { return sendTime; }
    public void setSendTime(String dateTime) throws ParseException {
        sendTime.setTime(DateFormat.getInstance().parse(dateTime));
    }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrequency() {
        return frequency;
    }
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isFilled() {
        return recipientInfo != null && recipientNumber != null;
    }

    public String getSendTimeAsString() {
        return DateFormat.getInstance().format(sendTime.getTime());
    }

    public static Message getInstanceFromCursor(Cursor messageCursor){
        Message parsedMessage = new Message();
        messageCursor.moveToFirst();
        parsedMessage.setId(messageCursor.getInt(messageCursor.getColumnIndex(_ID)));
        parsedMessage.setMessage(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_MESSAGE)));
        parsedMessage.setFrequency(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_FREQUENCY)));
        parsedMessage.setRecipientNumber(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_RECIPIENT_NUMBER)));
        parsedMessage.setRecipientInfo(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_RECIPIENT_NAME)));
        try {
            parsedMessage.setSendTime(messageCursor
                    .getString(messageCursor.getColumnIndex(COLUMN_NAME_SEND_TIME)));
        } catch (ParseException e) {
            Log.e("ParseException", e.getMessage());
        }
        messageCursor.close();
        return parsedMessage;
    }
}
