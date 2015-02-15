package com.ksp.nudge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;

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
        NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(context);
        Cursor sendTimeCursor = databaseHelper.getSendTimesFromDatabase();

        //Register times in OS for SendMessageService to check for outstanding messages
        sendTimeCursor.moveToFirst();
        while (!sendTimeCursor.isAfterLast()){
            String dateTime = sendTimeCursor.getString(sendTimeCursor.
                    getColumnIndex(NudgeMessageEntry.COLUMN_NAME_SEND_TIME));
            Calendar dateTimeCalendar = Calendar.getInstance();
            try {
                dateTimeCalendar.setTime(DateFormat.getInstance().parse(dateTime));
                SendMessageService.setServiceAlarm(context, dateTimeCalendar);
            } catch (ParseException e) {
                Log.e(e.getMessage(), "Rescheduling Message on Reboot Error");
            }
            sendTimeCursor.moveToNext();
        }
        sendTimeCursor.close();
    }
}
