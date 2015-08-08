package com.ksp.nudge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_SEND_TIME;

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
        Cursor sendTimeCursor = new NudgeDatabaseHelper(context).getSendTimesFromDatabase();

        //Register times in OS for SendMessageService to check for outstanding messages
        sendTimeCursor.moveToFirst();
        while (!sendTimeCursor.isAfterLast()){
            try {
                SendMessageService.setServiceAlarm(context, sendTimeCursor.getString(sendTimeCursor.
                        getColumnIndex(COLUMN_NAME_SEND_TIME)));
            } catch (ParseException e) {
                Log.e(e.getMessage(), "Rescheduling Message on Reboot Error");
            }
            sendTimeCursor.moveToNext();
        }
        sendTimeCursor.close();
    }
}
