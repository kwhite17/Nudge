package com.ksp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;

public class NudgeMessagesDbHelper extends SQLiteOpenHelper {
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

    public NudgeMessagesDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
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
     * @param name, the name of the message recipient
     * @param number, the number of the message recipient
     * @param msg, the contents of the message
     * @param time, what time the message will be sent
     * @param frequency, how often the message should be sent
     * @return whether or not message insertion was successful
     */
    public String writeMsgToDb(String name, String number, String msg,
            String time, String frequency) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues rowValues = new ContentValues();

        rowValues.put(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER, number);
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME, name);
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_SEND_TIME, time);
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_MESSAGE, msg);
        rowValues.put(NudgeMessageEntry.COLUMN_NAME_FREQUENCY, frequency);

        long insertId = db.insert(NudgeMessageEntry.TABLE_NAME, null, rowValues);
        return (insertId == -1) ? "Insertion failed": "Insertion successful";
    }
    
    /**
     * Reads all the message from the database
     * @return a SparseArray containing all the messages in the database
     */
    public SparseArray<String> readMsgsfromDb() {
        SQLiteDatabase database = this.getReadableDatabase();
        SparseArray<String> msgMap = new SparseArray<String>();
        String[] projection = {
                NudgeMessageEntry._ID,
                NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME,
                NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER,
                NudgeMessageEntry.COLUMN_NAME_SEND_TIME,
                NudgeMessageEntry.COLUMN_NAME_MESSAGE,
        };
        String sortOrder = NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME + " DESC";
        Cursor msgCursor = database.query(NudgeMessageEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        msgCursor.moveToFirst();
        while (!msgCursor.isAfterLast()){
            String recipient = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME));
            String message = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_MESSAGE));
            String sendDate = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_SEND_TIME));

            msgMap.put(msgCursor.getInt(msgCursor.getColumnIndex(NudgeMessageEntry._ID)), MessageHandler.formatMessage(recipient, message, sendDate));

            msgCursor.moveToNext();
        }
        msgCursor.close();
        return msgMap;
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