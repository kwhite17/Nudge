package com.ksp.nudge;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.message.NudgeInfo;
import com.ksp.message.MessageHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME;
import static com.ksp.message.MessageHandler.sendMessage;

public class SendMessageService extends IntentService {
    private static final String SERVICE_NAME = "MESSAGE_SERVICE";
    public SendMessageService() {
        super(SERVICE_NAME);
    }

    /**
     *
     * @param sendDate, the time the NudgeInfo is to be sent
     * @return a boolean indicating if it is time for the message to be sent
     * @throws ParseException
     */
    private static boolean isOutstandingMessage(Date sendDate) throws ParseException {
        return sendDate.compareTo(new Date()) < 0;
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
        Cursor messageCursor = db.query(TABLE_NAME, null, null, null, null,
                null, sortOrder);

        NudgeInfo[] currentNudges = NudgeInfo.getMessagesFromCursor(messageCursor);
        for (NudgeInfo currentNudge : currentNudges) {
            if (isOutstandingMessage(NudgeInfo.NUDGE_DATE_FORMAT.parse(currentNudge.getSendTimeAsString()))) {
                sendMessage(this, currentNudge.getRecipientNumber(), currentNudge.getMessage());
                if (databaseHelper.updateNudge(currentNudge)) {
                    setServiceAlarm(this, MessageHandler.getNextSend(currentNudge.getSendTime(),
                            currentNudge.getFrequency()));
                }
            }
        }
    }

    /**
     * Is responsible for scheduling the next message
     * @param messageContext, the context in which this function is called
     * @param messageTime, the time for the next message to be scheduled
     */
    public static void setServiceAlarm(Context messageContext, String messageTime) throws ParseException {
        DateFormat formatter = (DateFormat) NudgeInfo.NUDGE_DATE_FORMAT.clone();
        Intent sendMessageIntent = new Intent(messageContext, SendMessageService.class);
        PendingIntent pendingMessageIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pendingMessageIntent = PendingIntent.getService(messageContext,
                    (int) Instant.now().toEpochMilli(), sendMessageIntent, PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingMessageIntent = PendingIntent.getService(messageContext,
                    (int) System.currentTimeMillis(), sendMessageIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        AlarmManager messageAlarm = (AlarmManager)
                messageContext.getSystemService(Context.ALARM_SERVICE);
        if (messageAlarm != null) {
            messageAlarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, formatter.parse(messageTime).getTime(),
                    pendingMessageIntent);
        }
    }
}
