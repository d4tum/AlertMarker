package com.example.alertmarker;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class ProximityAlertReceiver extends BroadcastReceiver {
	private AlertMarkerApplication app;
	private String key = LocationManager.KEY_PROXIMITY_ENTERING;

	private Notification createNotification() {
		Notification notification = new Notification();

		notification.icon = R.drawable.ic_stat_proximity_alert_notification;
		notification.when = System.currentTimeMillis();

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		return notification;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean entering = intent.getBooleanExtra(key, false);

		if (entering) {
			int id = (int) intent.getLongExtra(Constants.EXTRA_PROX_ID_KEY, -1);
			int requestCode = intent.getIntExtra(
					Constants.EXTRA_PROX_REQUEST_CODE, 0);

			Log.d(getClass().getSimpleName(), "entering, id: " + id
					+ " requestCode: " + requestCode);

			app = (AlertMarkerApplication) context.getApplicationContext();
			ArrayList<Position> positions = app.getPositions();
			String address = new String();

			for (Position position : positions) {
				if (id == position.getId()) {
					address = position.getAddress();
					intent = createFindAlertMarkerIntent(context,
							MainActivity.class, position);
					Log.d(getClass().getSimpleName(),
							"entering, id: " + id + " " + position.getId()
									+ " " + position.getAddress());

				}
			}

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = createNotification();
			notification.tickerText = address;
			notification.setLatestEventInfo(context, "AlertMarker", address,
					pendingIntent);
			notificationManager.notify(id, notification);
		}
	}

	private Intent createFindAlertMarkerIntent(Context context,
			Class<MainActivity> cls, Position position) {

		Bundle bundle = new Bundle();

		bundle.putDouble("latitude", position.getLatitude());
		bundle.putDouble("longitude", position.getLongitude());

		Intent intent = new Intent(context, cls);
		intent.setAction(Constants.ACTION_FIND_ALERT_MARKER);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		intent.putExtras(bundle);
		return intent;
	}

}