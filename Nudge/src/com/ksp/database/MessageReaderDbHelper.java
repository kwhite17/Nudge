package com.ksp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ksp.database.MessageReaderContract.MessageEntry;

public class MessageReaderDbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MessageReader.db";
    
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
	    "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
	    MessageEntry._ID + " INTEGER PRIMARY KEY," +
	    MessageEntry.COLUMN_NAME_RECIPIENT + TEXT_TYPE + COMMA_SEP +
	    MessageEntry.COLUMN_NAME_SEND_TIME + TEXT_TYPE + COMMA_SEP +
	    MessageEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP +
	    MessageEntry.COLUMN_NAME_FREQUENCY + TEXT_TYPE +
	    " )";
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME;
	
	public MessageReaderDbHelper(Context context){
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

}
