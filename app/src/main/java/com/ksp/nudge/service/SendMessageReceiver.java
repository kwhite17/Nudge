package com.ksp.nudge.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SendMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SendMessageService.class));
    }
}
