package com.ksp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ksp.message.NudgeInfo;
import com.ksp.nudge.ActiveNudgesActivity;
import com.ksp.nudge.NudgeCursorAdapter;

import java.text.DateFormat;
import java.text.ParseException;

import static android.provider.BaseColumns._ID;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_FREQUENCY;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_MESSAGE;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NAME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME;
import static com.ksp.message.MessageHandler.getNextSend;

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
        SQLiteDatabase database = getReadableDatabase();
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
        SQLiteDatabase database = getReadableDatabase();
        String[] projection = new String[]{COLUMN_NAME_SEND_TIME};
        String sortOrder = COLUMN_NAME_SEND_TIME + " DESC";
        return database.query(TABLE_NAME, projection,
                null, null, null, null, sortOrder);
    }

    /**
     *
     * @param id, the id of the nudge in the database
     * @return a cursor containing the information that represents a nudge NudgeInfo
     */
    public Cursor getNudgeEntry(String id){
        SQLiteDatabase database = getReadableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { id };
        return database.query(TABLE_NAME, null,
                selection, selectionArgs, null, null, null);
    }

    /**
     *
     * @param nudge, a NudgeInfo with the updated information
     * @return a String dictating whether the nudge was successfully updated
     */
    public String updateExistingMessage(NudgeInfo nudge) {
        SQLiteDatabase database = getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { nudge.getId() };
        long updateResult = database.update(TABLE_NAME, populateMessageFields(nudge),
                selection, selectionArgs);
        database.close();
        return updateResult == -1 ? "Update failed" : "Update completed";
    }

    /**
     * Updates the send time of a recurring message in the database
     * @param nudge, the message containing the info needed to update the send time
     */
    public void updateSendTime(NudgeInfo nudge) {
        SQLiteDatabase database = getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { nudge.getId() };
        ContentValues sendTime = new ContentValues();
        sendTime.put(COLUMN_NAME_SEND_TIME, getNextSend(nudge.getSendTime(), nudge.getFrequency()));
        database.update(TABLE_NAME, sendTime, selection, selectionArgs);
        database.close();
    }

    /**
     * Writes the contents of a message to the database
     * @param nudge, a NudgeInfo containing the data to be inserted into the database
     * @return whether or not message insertion was successful
     */
    public String writeMessageToDatabase(NudgeInfo nudge) {
        SQLiteDatabase database = getWritableDatabase();
        long insertionResult = database.insert(TABLE_NAME, null, populateMessageFields(nudge));
        database.close();
        return insertionResult == -1 ? "Insertion failed": "Insertion successful";
    }
    
    /**
     * Deletes a message from the database based on its id
     * @param id, the id of the message to delete
     */
    public void deleteMessage(String id){
        SQLiteDatabase database = getWritableDatabase();
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { id };
        database.delete(TABLE_NAME, selection, selectionArgs);
        database.close();
    }

    /**
     *
     * @param nudge, a NudgeInfo containing the data to be inserted into the database
     * @return ContentValues mapping nudge to the appropriate database columns
     */
    private ContentValues populateMessageFields(NudgeInfo nudge) {
        ContentValues messageFields = new ContentValues();
        messageFields.put(COLUMN_NAME_RECIPIENT_NUMBER, nudge.getRecipientNumber());
        messageFields.put(COLUMN_NAME_RECIPIENT_NAME, nudge.getRecipientInfo());
        messageFields.put(COLUMN_NAME_SEND_TIME,
                DateFormat.getInstance().format(nudge.getSendTime().getTime()));
        messageFields.put(COLUMN_NAME_MESSAGE, nudge.getMessage());
        messageFields.put(COLUMN_NAME_FREQUENCY, nudge.getFrequency());
        return messageFields;
    }

    public boolean updateNudge(NudgeInfo currentNudge) throws ParseException {
        if (currentNudge.getFrequency().equals("Once")) {
            deleteMessage(currentNudge.getId());
            NudgeCursorAdapter nudgeAdapter = ActiveNudgesActivity.getNudgeAdapter();
            if (nudgeAdapter != null) {
                nudgeAdapter.refreshAdapter(this);
            }
            return false;
        }
        updateSendTime(currentNudge);
        return true;
    }
}
