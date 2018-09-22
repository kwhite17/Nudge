package com.ksp.message;

import android.database.Cursor;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import static android.provider.BaseColumns._ID;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_FREQUENCY;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME;

/**
 * The NudgeInfo class is data structure responsible for maintaining all the information a user
 * provides about the Nudge they are attempting to create.
 */
public class NudgeInfo {
    private String id;
    private String recipientInfo;
    private String recipientNumber;
    private Instant sendTime = Instant.ofEpochMilli(DateTimeZone.getDefault()
            .convertLocalToUTC(System.currentTimeMillis(), false));
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

    public Instant getSendTime() {
        return sendTime;
    }
    private void setSendTime(String dateTime) {
        sendTime = Instant.ofEpochMilli(Long.parseLong(dateTime));
    }
    public void setSendTime(long instant) {
        sendTime = sendTime.withMillis(instant);
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

    /**
     *
     * @param messageCursor, a cursor containing all the information necessary to create a NudgeInfo
     *                       instance
     * @return a new NudgeInfo instance with the data from messageCursor
     */
    public static NudgeInfo getMessageFromCursor(Cursor messageCursor){
        messageCursor.moveToFirst();
        NudgeInfo parsedNudgeInfo = buildMessageFromRow(messageCursor);
        messageCursor.close();
        return parsedNudgeInfo;
    }

    /**
     *
     * @param messageCursor, a database cursor containing the data necessary to build messages
     * @return messages, a NudgeInfo array built from the database rows in the cursor
     */
    public static NudgeInfo[] getMessagesFromCursor(Cursor messageCursor){
        NudgeInfo[] nudgeInfos = new NudgeInfo[messageCursor.getCount()];
        messageCursor.moveToFirst();
        while (!messageCursor.isAfterLast()) {
            nudgeInfos[messageCursor.getPosition()] = buildMessageFromRow(messageCursor);
            messageCursor.moveToNext();
        }
        messageCursor.close();
        return nudgeInfos;
    }

    /**
     *
     * @param messageCursor, a cursor pointed at a row needed to build this instance of NudgeInfo
     * @return an instance of NudgeInfo containing the data from the row the cursor pointed to
     */
    private static NudgeInfo buildMessageFromRow(Cursor messageCursor) {
        NudgeInfo parsedNudgeInfo = new NudgeInfo();
        parsedNudgeInfo.setId(Integer
                .toString(messageCursor.getInt(messageCursor.getColumnIndex(_ID))));
        parsedNudgeInfo.setMessage(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_MESSAGE)));
        parsedNudgeInfo.setFrequency(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_FREQUENCY)));
        parsedNudgeInfo.setRecipientNumber(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_RECIPIENT_NUMBER)));
        parsedNudgeInfo.setRecipientInfo(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_RECIPIENT_NAME)));
        parsedNudgeInfo.setSendTime(messageCursor
                .getString(messageCursor.getColumnIndex(COLUMN_NAME_SEND_TIME)));
        return parsedNudgeInfo;
    }
}
