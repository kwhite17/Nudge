package com.ksp.nudge;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ksp.database.MessageHandler;
import com.ksp.database.NudgeMessagesContract.NudgeMessageEntry;
import com.ksp.database.NudgeMessagesDbHelper;

import java.text.ParseException;
import java.util.Calendar;

public class SendMessageService extends IntentService {
    public static final String SERVICE_NAME = "MESSAGE_SERVICE";
    public SendMessageService() {
        super(SERVICE_NAME);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        try {
            deliverOustandingMessages();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            deliverOustandingMessages();
            this.stopSelf();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Delivers any messages queued to be sent and the times for when the message
     * is supposed to be sent
     * @throws ParseException
     */
    public void deliverOustandingMessages() throws ParseException{
        NudgeMessagesDbHelper dbHelper = new NudgeMessagesDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                NudgeMessageEntry._ID,
                NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER,
                NudgeMessageEntry.COLUMN_NAME_SEND_TIME,
                NudgeMessageEntry.COLUMN_NAME_MESSAGE,
                NudgeMessageEntry.COLUMN_NAME_FREQUENCY,
        };
        String sortOrder = NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER + " DESC";
        Cursor msgCursor = db.query(NudgeMessageEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        msgCursor.moveToFirst();
        while (!msgCursor.isAfterLast()){
            String id = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry._ID));
            String recipient = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER));
            String message = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_MESSAGE));
            String sendDate = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_SEND_TIME));
            String frequency = msgCursor.getString(msgCursor.getColumnIndex(NudgeMessageEntry.COLUMN_NAME_FREQUENCY));

            if (MessageHandler.isOutstandingMessage(sendDate)){
                MessageHandler.sendMessage(recipient, message);
                NudgeMessagesDbHelper databaseHelper = new NudgeMessagesDbHelper(this);
                if (frequency.equals("Once")){
                    databaseHelper.deleteMessage(id);
                    ActiveNudgesActivity.getNudgeAdapter().refreshAdapter(databaseHelper);
                } else{
                    databaseHelper.updateSendTime(id, sendDate, frequency, this);
                }
            }

            msgCursor.moveToNext();
        }
        msgCursor.close();
    }
    
    /**
     * Is responsible for scheduling the next message
     * @param messageContext, the context in which this function is called
     * @param messageTime, the time for the next message to be scheduled
     */
    public static void setServiceAlarm(Context messageContext, Calendar messageTime) {
        Intent sendMessageIntent = new Intent(messageContext, SendMessageService.class);
        PendingIntent pendingMessageIntent = PendingIntent.getService(messageContext, 
                (int)System.currentTimeMillis(), sendMessageIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager messageAlarm = (AlarmManager)
                messageContext.getSystemService(Context.ALARM_SERVICE);
        messageAlarm.set(AlarmManager.RTC_WAKEUP, messageTime.getTimeInMillis(), pendingMessageIntent);        
    }
}
