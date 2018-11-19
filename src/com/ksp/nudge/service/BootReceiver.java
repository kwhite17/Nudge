package com.ksp.nudge.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ksp.nudge.db.NudgeDatabaseHelper;
import com.ksp.nudge.model.Nudge;


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
        //Register times in OS for SendMessageService to check for outstanding messages
        for (Nudge outstandingNudge : NudgeDatabaseHelper.getOutstandingNudges()) {
            SendMessageService.setServiceAlarm(context, outstandingNudge.getNudgeConfig().getSendTime());
        }
    }
}