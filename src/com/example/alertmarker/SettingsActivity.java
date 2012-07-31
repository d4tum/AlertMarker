package com.example.alertmarker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private boolean proxAlertsActive;
	private ProximityAlertService proximityAlertService;
	private boolean bound;
	private SharedPreferences preferences;

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		Preference proxAlertsPref = (CheckBoxPreference) findPreference("prox_alerts_active");
		proxAlertsPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						// Turn on / off prox alerts
						if (proxAlertsActive) {
							proximityAlertService.removeAllProximityAlerts();
							stopService(new Intent(SettingsActivity.this,
									ProximityAlertService.class));
							Toast.makeText(SettingsActivity.this,
									"Proximity alerts OFF", Toast.LENGTH_LONG)
									.show();
						} else {
							startService(new Intent(SettingsActivity.this,
									ProximityAlertService.class));
							proximityAlertService.setUpProximityAlerts();
							Toast.makeText(SettingsActivity.this,
									"Proximity alerts ON", Toast.LENGTH_LONG)
									.show();
						}
						proxAlertsActive = !proxAlertsActive;
						return false;
					}
				});
	}

	@Override
	protected void onStart() {
		super.onStart();
		doBindService();

	}

	protected void onResume() {
		super.onResume();
		preferences.registerOnSharedPreferenceChangeListener(this);
		proxAlertsActive = preferences.getBoolean("prox_alerts_active", false);
	}

	protected void onPause() {
		super.onPause();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		doUnbindService();
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// Create a binder object used to call methods in the service
			proximityAlertService = ((ProximityAlertService.LocalBinder) service)
					.getService();
			Log.i(getClass().getSimpleName(),
					"Service is alive and connected to Activity");
		}

		public void onServiceDisconnected(ComponentName className) {
			proximityAlertService = null;
		}
	};

	private void doBindService() {
		bindService(new Intent(SettingsActivity.this,
				ProximityAlertService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
		bound = true;
	}

	private void doUnbindService() {
		if (bound) {
			// Detach our existing connection.
			unbindService(serviceConnection);
			bound = false;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Re-create all alerts if the radius is changed
		if (key.equals("prox_alert_radius")) {
			if (proxAlertsActive) {
				proximityAlertService.removeAllProximityAlerts();
				proximityAlertService.setUpProximityAlerts();
			}
		}

	}

}