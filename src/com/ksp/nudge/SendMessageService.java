package com.ksp.nudge;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.message.MessageHandler;
import com.ksp.message.NudgeInfo;

import org.joda.time.Instant;

import java.text.ParseException;

import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME;
import static com.ksp.message.MessageHandler.sendMessage;

public class SendMessageService extends JobService {
    private static final long MAX_DELAY_MILLIS = 300000;

    @Override
    public boolean onStartJob(final JobParameters params) {
        try {
            deliverOutstandingMessages();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    /**
     *
     * @param instant, the time the NudgeInfo is to be sent
     * @return a boolean indicating if it is time for the message to be sent
     */
    private static boolean isOutstandingMessage(Instant instant) {
        return !instant.isAfterNow();
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
            if (isOutstandingMessage(currentNudge.getSendTime())) {
                sendMessage(this, currentNudge.getRecipientNumber(), currentNudge.getMessage());
                if (databaseHelper.updateNudge(currentNudge)) {
                    setServiceAlarm(this, MessageHandler.getNextSend(currentNudge.getSendTime(),
                            currentNudge.getFrequency()).getMillis());
                }
            } else {
                setServiceAlarm(this, currentNudge.getSendTime().getMillis());
            }
        }
    }

    /**
     * Is responsible for scheduling the next message
     * @param messageContext, the context in which this function is called
     * @param messageTime, the time for the next message to be scheduled
     */
    public static void setServiceAlarm(Context messageContext, long messageTime) throws ParseException {
        long millisBefore = Math.max(messageTime - Instant.now().getMillis(), 0);
        JobInfo jobInfo = new JobInfo.Builder(messageContext.hashCode(), new ComponentName(messageContext, SendMessageService.class))
                .setPersisted(true)
                .setMinimumLatency(millisBefore)
                .setOverrideDeadline(millisBefore + MAX_DELAY_MILLIS)
                .build();
        JobScheduler jobScheduler = (JobScheduler) messageContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}
