package com.example.alertmarker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (preferences.getBoolean("prox_alerts_active", false)) {
			Intent startServiceIntent = new Intent(context,
					ProximityAlertService.class);
			context.startService(startServiceIntent);
		}
	}

}
