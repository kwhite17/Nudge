package com.ksp.nudge;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.ksp.database.NudgeDatabaseHelper;
import com.ksp.message.MessageHandler;
import com.ksp.message.NudgeInfo;

import org.joda.time.Instant;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.COLUMN_NAME_RECIPIENT_NUMBER;
import static com.ksp.database.NudgeMessagesContract.NudgeMessageEntry.TABLE_NAME;
import static com.ksp.message.MessageHandler.sendMessage;

public class SendMessageWorker extends Worker {

    public SendMessageWorker(@NonNull Context context,
                             @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            deliverOutstandingMessages();
        } catch (ParseException e) {
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    /**
     * Delivers any messages queued to be sent and updates the send times for recurring messages
     * @throws ParseException
     */
    private void deliverOutstandingMessages() throws ParseException{
        NudgeDatabaseHelper databaseHelper = new NudgeDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sortOrder = COLUMN_NAME_RECIPIENT_NUMBER + " DESC";
        Cursor messageCursor = db.query(TABLE_NAME, null, null, null, null,
                null, sortOrder);

        NudgeInfo[] currentNudges = NudgeInfo.getMessagesFromCursor(messageCursor);
        for (NudgeInfo currentNudge : currentNudges) {
            if (isOutstandingMessage(currentNudge.getSendTime())) {
                sendMessage(getApplicationContext(), currentNudge.getRecipientNumber(), currentNudge.getMessage());
                if (databaseHelper.updateNudge(currentNudge)) {
                    scheduleMessage(currentNudge.getId(), MessageHandler.getNextSend(currentNudge.getSendTime(),
                            currentNudge.getFrequency()).getMillis());
                }
            }
        }
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
     * Is responsible for scheduling the next message
     * @param messageTime, the time for the next message to be scheduled
     */
    static void scheduleMessage(String nudgeId, long messageTime) {
        long millisBefore = Math.max(messageTime - Instant.now().getMillis(), 0);
        WorkManager.getInstance().cancelAllWorkByTag(nudgeId);
        OneTimeWorkRequest automatedMessageWork = new OneTimeWorkRequest.Builder(SendMessageWorker.class)
                .setInitialDelay(millisBefore, TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                        .build())
                .addTag(nudgeId)
                .build();
        WorkManager.getInstance().enqueue(automatedMessageWork);
    }

}
