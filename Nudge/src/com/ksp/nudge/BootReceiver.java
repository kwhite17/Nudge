package com.ksp.nudge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startServiceIntent = new Intent(context,SendMessageService.class);
		context.startService(startServiceIntent);
	}
}
