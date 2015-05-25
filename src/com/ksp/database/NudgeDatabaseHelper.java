package com.ksp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;
import com.ksp.message.Message;
import com.ksp.message.MessageHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import static android.provider.BaseColumns._ID;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_FREQUENCY;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME;

public class NudgeDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NudgeMessages.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_RECIPIENT_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_RECIPIENT_NUMBER + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_SEND_TIME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_FREQUENCY + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public NudgeDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     *
     * @return a cursor containing all message data in the database
     */
    public Cursor readMessagesFromDatabase(){
        SQLiteDatabase database = this.getReadableDatabase();
        String[] databaseColumns = {_ID,COLUMN_NAME_RECIPIENT_NAME,COLUMN_NAME_MESSAGE,
                COLUMN_NAME_SEND_TIME};
        String sortOrder = COLUMN_NAME_RECIPIENT_NAME + " DESC";
        return database.query(TABLE_NAME,
                databaseColumns, null, null, null, null, sortOrder);
    }

    /**
     *
     * @return A cursor containing all the message send times from the database
     */
    public Cursor getSendTimesFromDatabase(){
        SQLiteDatabase database = this.getReadableDatabase();
        String[] projection = new String[]{COLUMN_NAME_SEND_TIME};
        String sortOrder = COLUMN_NAME_SEND_TIME + " DESC";
        return database.query(TABLE_NAME, projection,
                null, null, null, null, sortOrder);
    }

    public Cursor getNudgeEntry(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { id };
        return database.query(TABLE_NAME, null,
                selection, selectionArgs, null, null, null);
    }

    public String updateExistingMessage(Message nudge) {
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { Integer.toString(nudge.getId()) };
        long updateResult = database.update(TABLE_NAME, buildMessageProperties(nudge),
                selection, selectionArgs);
        return updateResult == -1 ? "Update failed" : "Update completed";
    }

    /**
     * Updates the send time of a recurring message in the database
     * @param id, the id of the database entry to update
     * @param sendDate, the current send time of the message
     * @param frequency, how often the message is supposed to be sent
     */
    public Calendar updateSendTime(String id, String sendDate, String frequency)
            throws ParseException {
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { id };
        ContentValues sendTime = new ContentValues();
        String nextSendTime = MessageHandler.getNextSend(sendDate, frequency);
        sendTime.put(COLUMN_NAME_SEND_TIME, nextSendTime);
        database.update(TABLE_NAME, sendTime, selection, selectionArgs);
        Calendar nextSendCalendar = Calendar.getInstance();
        nextSendCalendar.setTime(DateFormat.getInstance().parse(nextSendTime));
        database.close();
        return nextSendCalendar;
    }

    /**
     * Writes the contents of a message to the database
     * @param nudge, the contents of the message and how often it should be sent
     * @return whether or not message insertion was successful
     */
    public String writeMessageToDatabase(Message nudge) {
        SQLiteDatabase database = this.getWritableDatabase();
        long insertionResult = database.insert(TABLE_NAME, null, buildMessageProperties(nudge));
        database.close();
        return insertionResult == -1 ? "Insertion failed": "Insertion successful";
    }
    
    /**
     * Deletes a message from the database based on its id
     * @param id, the id of the message to delete
     */
    public void deleteMessage(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { id };
        database.delete(TABLE_NAME, selection, selectionArgs);
        database.close();
    }

    private ContentValues buildMessageProperties(Message nudge) {
        ContentValues messageProperties = new ContentValues();
        messageProperties.put(COLUMN_NAME_RECIPIENT_NUMBER, nudge.getRecipientNumber());
        messageProperties.put(COLUMN_NAME_RECIPIENT_NAME, nudge.getRecipientInfo());
        messageProperties.put(COLUMN_NAME_SEND_TIME,
                DateFormat.getInstance().format(nudge.getSendTime().getTime()));
        messageProperties.put(COLUMN_NAME_MESSAGE, nudge.getMessage());
        messageProperties.put(COLUMN_NAME_FREQUENCY, nudge.getFrequency());
        return messageProperties;
    }
}
