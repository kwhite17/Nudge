package com.ksp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;
import com.ksp.message.Message;
import com.ksp.message.MessageHandler;

public class NudgeDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NudgeMessages.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NudgeMessageEntry.TABLE_NAME + " (" +
                    NudgeMessageEntry._ID + " INTEGER PRIMARY KEY," +
                    NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME + TEXT_TYPE + COMMA_SEP +
                    NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER + TEXT_TYPE + COMMA_SEP +
                    NudgeMessageEntry.COLUMN_NAME_SEND_TIME + TEXT_TYPE + COMMA_SEP +
                    NudgeMessageEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP +
                    NudgeMessageEntry.COLUMN_NAME_FREQUENCY + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NudgeMessageEntry.TABLE_NAME;

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
     * Writes the contents of a message to the database
     * @param nudge, the contents of the message and how often it should be sent
     * @param time, what time the message will be sent
     * @return whether or not message insertion was successful
     */
    public String writeMessageToDatabase(Message nudge, String time) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues rowValues = new ContentValues();
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER, nudge.getRecipientNumber());
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME, nudge.getRecipientInfo());
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_SEND_TIME, time);
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_MESSAGE, nudge.getMessage());
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_FREQUENCY, nudge.getFrequency());
        long insertId = database.insert(NudgeMessageEntry.TABLE_NAME, null, rowValues);
        return (insertId == -1) ? "Insertion failed": "Insertion successful";
    }

    /**
     *
     * @return a cursor containing all message data in the database
     */
    public Cursor readMessagesFromDatabase(){
        SQLiteDatabase database = this.getReadableDatabase();
        String[] databaseColumns = {
                NudgeMessagesContract.NudgeMessageEntry._ID,
                NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME,
                NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE,
                NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME
        };
        String sortOrder = NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME + " DESC";
        Cursor databaseCursor = database.query(NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME,
                databaseColumns, null, null, null, null, sortOrder);
        return databaseCursor;
    }

    /**
     *
     * @return A cursor containing all the message send times from the database
     */
    public Cursor getSendTimesFromDatabase(){
        SQLiteDatabase database = this.getReadableDatabase();
        String[] projection = new String[]{NudgeMessageEntry.COLUMN_NAME_SEND_TIME};
        String sortOrder = NudgeMessageEntry.COLUMN_NAME_SEND_TIME + " DESC";
        return database.query(NudgeMessageEntry.TABLE_NAME, projection,
                null, null, null, null, sortOrder);
    }
    
    /**
     * Deletes a message from the database based on its id
     * @param id, the id of the message to delete
     */
    public void deleteMessage(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = NudgeMessageEntry._ID + " LIKE ?";
        String[] selectionArgs = { id };
        database.delete(NudgeMessageEntry.TABLE_NAME, selection, selectionArgs);
    }
    
    /**
     * Updates the send time of a recurring message in the database
     * @param messageContext the context in which this method is being called
     * @param id, the id of the database entry to update
     * @param sendDate, the current send time of the message
     * @param frequency, how often the message is supposed to be sent
     */
    public void updateSendTime(String id, String sendDate, String frequency, Context messageContext){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = NudgeMessageEntry._ID + " LIKE ?";
        String[] selectionArgs = { id };
        ContentValues sendTime = new ContentValues();
        sendTime.put(NudgeMessageEntry.COLUMN_NAME_SEND_TIME, MessageHandler.getNextSend(sendDate, frequency, messageContext));
        db.update(NudgeMessageEntry.TABLE_NAME, sendTime, selection, selectionArgs);
    }
}
