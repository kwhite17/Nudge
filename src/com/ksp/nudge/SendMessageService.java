package com.ksp.nudge;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.message.Message;
import com.ksp.message.MessageHandler;

import java.text.ParseException;
import java.util.Calendar;

import static android.provider.BaseColumns._ID;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME;

public class SendMessageService extends IntentService {
    private static final String SERVICE_NAME = "MESSAGE_SERVICE";
    public SendMessageService() {
        super(SERVICE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        try {
            deliverOutstandingMessages();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            deliverOutstandingMessages();
            this.stopSelf();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delivers any messages queued to be sent and updates the send times for recurring messages
     * @throws ParseException
     */
    private void deliverOutstandingMessages() throws ParseException{
        NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sortOrder = COLUMN_NAME_RECIPIENT_NUMBER + " DESC";
        Cursor messageCursor = db.query(TABLE_NAME, null, null, null, null, null, sortOrder);

        Message[] currentNudges = Message.getMessagesFromCursor(messageCursor);
        for (Message currentNudge: currentNudges) {
            String id = Integer.toString(currentNudge.getId());
            if (MessageHandler.isOutstandingMessage(currentNudge.getSendTimeAsString())) {
                MessageHandler.sendMessage(currentNudge.getRecipientNumber(),
                        currentNudge.getMessage());
                if (currentNudge.getFrequency().equals("Once")) {
                    databaseHelper.deleteMessage(id);
                    ActiveNudgesActivity.getNudgeAdapter().refreshAdapter(databaseHelper);
                } else {
                    Calendar nextSendTime = databaseHelper.updateSendTime(id,
                            currentNudge.getSendTimeAsString(),
                            currentNudge.getFrequency());
                    setServiceAlarm(this, nextSendTime);
                }
            }
        }
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
        messageAlarm.set(AlarmManager.RTC_WAKEUP, messageTime.getTimeInMillis(),
                pendingMessageIntent);
    }
}
