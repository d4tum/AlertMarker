package com.example.alertmarker;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class ProximityAlertService extends Service {
	private final IBinder binder = new LocalBinder();
	private LocationManager locationManager;
	private boolean gps_enabled;
	private boolean network_enabled;
	private int radius;
	private HashMap<Long, PendingIntent> pendingIntents = new HashMap<Long, PendingIntent>();
	private SharedPreferences preferences;
	private AlertMarkerApplication app;
	private ArrayList<Position> positions;
	private ProximityAlertReceiver proximityAlertReceiver;
	private IntentFilter filter;

	public class LocalBinder extends Binder {
		public ProximityAlertService getService() {
			return ProximityAlertService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		app = (AlertMarkerApplication) getApplication();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				proxAlertCRUDReciever,
				new IntentFilter(Constants.ACTION_PROX_ALERT_CRUD_BROADCAST));
		if (locationManager == null)
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		filter = new IntentFilter(Constants.ACTION_PROXIMITY_ALERT);
		proximityAlertReceiver = new ProximityAlertReceiver();
		registerReceiver(proximityAlertReceiver, filter);

		Handler handler = new Handler();
		handler.post(new Runnable() {

			public void run() {
				setUpLocationListeners();
			}
		});

		if (preferences.getBoolean("prox_alerts_active", false)) {
			setUpProximityAlerts();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locationManager.removeUpdates(locationListenerGps);
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			locationManager.removeUpdates(locationListenerNetwork);
		unregisterReceiver(proximityAlertReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				proxAlertCRUDReciever);
		Log.i(getClass().getSimpleName(), "Service onDestroy");
	}

	private void setUpLocationListeners() {

		try {
			gps_enabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		// don't start listeners if no provider is enabled
		if (!gps_enabled && !network_enabled) {
			Toast.makeText(getApplicationContext(),
					"No location receivers enabled", Toast.LENGTH_LONG).show();
			return;
		}

		if (gps_enabled)
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 1, locationListenerGps);
		if (network_enabled)
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1000, 1,
					locationListenerNetwork);

	}

	LocationListener locationListenerGps = new LocationListener() {

		public void onLocationChanged(Location location) {
		}

		public void onProviderDisabled(String provider) {
			locationManager.removeUpdates(this);
		}

		public void onProviderEnabled(String provider) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 1, locationListenerGps);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	LocationListener locationListenerNetwork = new LocationListener() {
		public void onLocationChanged(Location location) {
		}

		public void onProviderDisabled(String provider) {
			locationManager.removeUpdates(this);
		}

		public void onProviderEnabled(String provider) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1000, 1,
					locationListenerNetwork);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private void createProximityAlert(double latitude, double longitude,
			long id, int requestCode) {

		Intent intent = new Intent(Constants.ACTION_PROXIMITY_ALERT);
		intent.putExtra(Constants.EXTRA_PROX_ID_KEY, id);
		intent.putExtra(Constants.EXTRA_PROX_REQUEST_CODE, requestCode);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), requestCode, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		pendingIntents.put(id, pendingIntent);
		locationManager.addProximityAlert(latitude, longitude, radius, -1,
				pendingIntent);
		Log.d(getClass().getSimpleName(), "with radius: " + radius);
	}

	public void setUpProximityAlerts() {
		radius = Integer.valueOf(preferences.getString("prox_alert_radius",
				"1000"));

		positions = app.getPositions();
		for (int i = 0; i < positions.size(); i++) {
			Log.d(getClass().getSimpleName(), "Added prox alert - id: "
					+ positions.get(i).getId() + " address: "
					+ positions.get(i).getAddress());
			createProximityAlert(positions.get(i).getLatitude(),
					positions.get(i).getLongitude(), positions.get(i).getId(),
					i + 1);
		}
	}

	public void removeAllProximityAlerts() {
		positions = app.getPositions();
		for (Position position : positions) {
			removeProximityAlert(position.getId());
			Log.d("Removed prox alert for : ", "" + position.getId() + " "
					+ position.getAddress());
		}
		pendingIntents.clear();
	}

	public void removeProximityAlert(long id) {
		locationManager.removeProximityAlert(pendingIntents.get(id));
		pendingIntents.remove(id);
		Log.d(getClass().getSimpleName(), "Removed prox alert wtih id: " + id);
	}

	private BroadcastReceiver proxAlertCRUDReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			positions = app.getPositions();
			if (intent.getBooleanExtra("create_prox_alert", false)) {
				for (Position position : positions) {
					if (position.getId() == intent.getLongExtra("id", -1)) {
						createProximityAlert(position.getLatitude(),
								position.getLongitude(), position.getId(), 0);
						Log.d("Added prox alert for : ", "" + position.getId()
								+ " " + position.getAddress());
					}
				}
			}
			if (intent.getBooleanExtra("delete_prox_alert", false)) {
				removeProximityAlert((intent.getLongExtra("id", -1)));
				Log.d("Removed prox alert", "removed!");
			}
		}
	};

}