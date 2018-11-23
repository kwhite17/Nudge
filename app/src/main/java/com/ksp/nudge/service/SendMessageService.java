package com.ksp.nudge.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ksp.nudge.NudgeUtils;
import com.ksp.nudge.db.NudgeDatabaseHelper;
import com.ksp.nudge.model.Nudge;

import org.joda.time.Instant;

public class SendMessageService extends IntentService {
    private static final String SERVICE_NAME = "MESSAGE_SERVICE";
    public SendMessageService() {
        super(SERVICE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        deliverOutstandingMessages();
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        deliverOutstandingMessages();
        this.stopSelf();
    }

    /**
     * Delivers any messages queued to be sent and updates the send times for recurring messages
     */
    private void deliverOutstandingMessages() {
        for (Nudge currentNudge : NudgeDatabaseHelper.getOutstandingNudges()) {
                NudgeUtils.sendMessage(this, currentNudge.getRecipients(), currentNudge.getNudgeConfig().getMessage());
                if (NudgeDatabaseHelper.maybeUpdateNudge(currentNudge)) {
                    setServiceAlarm(this, NudgeUtils.getNextSend(currentNudge.getNudgeConfig().getSendTime(),
                            currentNudge.getNudgeConfig().getFrequency()));
                }
        }
    }

    /**
     * Is responsible for scheduling the next message
     * @param messageContext, the context in which this function is called
     * @param messageTime, the time for the next message to be scheduled
     */
    public static void setServiceAlarm(Context messageContext, Instant messageTime) {
        Intent sendMessageIntent = new Intent(messageContext, SendMessageReceiver.class);
        PendingIntent pendingMessageIntent = PendingIntent.getBroadcast(messageContext, (int) messageTime.getMillis(), sendMessageIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager messageAlarm = (AlarmManager)
                messageContext.getSystemService(Context.ALARM_SERVICE);
        if (messageAlarm != null) {
            messageAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, messageTime.getMillis(),
                    pendingMessageIntent);
        }
    }
}