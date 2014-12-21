package com.ksp.nudge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;
import com.ksp.database.NudgeMessagesDbHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }
    
    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     * 
     * Reschedules messages to be sent via alarm. Sends outstanding messages immediately.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        NudgeMessagesDbHelper databaseHelper = new NudgeMessagesDbHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String[] projection = new String[]{NudgeMessageEntry.COLUMN_NAME_SEND_TIME};
        String sortOrder = NudgeMessageEntry.COLUMN_NAME_SEND_TIME + " DESC";
        Cursor cursor = database.query(NudgeMessageEntry.TABLE_NAME, projection,
                null, null, null, null, sortOrder);
        
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            String dateTime = cursor.getString(cursor.getColumnIndex(projection[0]));
            Calendar dateTimeCalendar = Calendar.getInstance();
            try {
                dateTimeCalendar.setTime(DateFormat.getInstance().parse(dateTime));
                SendMessageService.setServiceAlarm(context, dateTimeCalendar);
            } catch (ParseException e) {
                Log.e(e.getMessage(), "Rescheduling Message on Reboot Error");
            }
            cursor.moveToNext();
        }
        cursor.close();
    }
}
