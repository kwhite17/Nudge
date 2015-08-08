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
    private String id;
    private String recipientInfo;
    private String recipientNumber;
    private Calendar sendTime = Calendar.getInstance();
    private String message = "";
    private String frequency = "Weekly";

    public String getId() { return id; }
    public void setId(String id) {
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

    /**
     *
     * @param messageCursor, a cursor containing all the information necessary to create a Message
     *                       instance
     * @return a new Message instance with the data from messageCursor
     */
    public static Message getMessageFromCursor(Cursor messageCursor){
        messageCursor.moveToFirst();
        Message parsedMessage = buildMessageFromRow(messageCursor);
        messageCursor.close();
        return parsedMessage;
    }

    /**
     *
     * @param messageCursor, a database cursor containing the data necessary to build messages
     * @return messages, a Message array built from the database rows in the cursor
     */
    public static Message[] getMessagesFromCursor(Cursor messageCursor){
        Message[] messages = new Message[messageCursor.getCount()];
        messageCursor.moveToFirst();
        while (!messageCursor.isAfterLast()) {
            messages[messageCursor.getPosition()] = buildMessageFromRow(messageCursor);
            messageCursor.moveToNext();
        }
        messageCursor.close();
        return messages;
    }

    /**
     *
     * @param messageCursor, a cursor pointed at a row needed to build this instance of Message
     * @return an instance of Message containing the data from the row the cursor pointed to
     */
    private static Message buildMessageFromRow(Cursor messageCursor) {
        Message parsedMessage = new Message();
        parsedMessage.setId(Integer
                .toString(messageCursor.getInt(messageCursor.getColumnIndex(_ID))));
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
        return parsedMessage;
    }
}
