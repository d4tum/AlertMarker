package com.example.alertmarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MainActivity extends SherlockMapActivity {
	protected static final int REASONABLE_ZOOM_LEVEL = 17;
	private MapView mapView;
	private MyLocationOverlay myLocationOverlay;
	private GestureDetectorOverlay gestureDetectorOverlay;
	private AlertDialog dialog;
	private Drawable drawable;
	private MarkersItemizedOverlay markersItemizedoverlay;
	private ArrayList<Position> positions;
	private AlertMarkerApplication app;
	private SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set the transparent ActionBar
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.ab_bg_black));
		setSupportProgressBarIndeterminateVisibility(false);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		app = (AlertMarkerApplication) getApplication();
		setUpViews();

	}

	private void setUpViews() {
		mapView = (MapView) findViewById(R.id.map);
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		mapView.setBuiltInZoomControls(true);

		gestureDetectorOverlay = new GestureDetectorOverlay(this,
				new OnGestureListener() {

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						return false;
					}

					@Override
					public void onShowPress(MotionEvent e) {
					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						return false;
					}

					@Override
					public void onLongPress(MotionEvent ev) {
						// Vibrate on a long press
						mapView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
						Projection p = mapView.getProjection();

						GeoPoint geoPoint = p.fromPixels((int) ev.getX(),
								(int) ev.getY());

						ReverseGeoCoderTask reverseGeoCoderTask = new ReverseGeoCoderTask();
						reverseGeoCoderTask.execute(geoPoint);
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						return false;
					}

					@Override
					public boolean onDown(MotionEvent e) {
						return false;
					}
				});

		mapView.getOverlays().add(gestureDetectorOverlay);

		drawable = MainActivity.this.getResources().getDrawable(
				R.drawable.ic_marker_pin);
		markersItemizedoverlay = new MarkersItemizedOverlay(drawable,
				MainActivity.this);

		drawItemizedOverlay();
	}

	// Used to find the marker from a proximity alert notification
	// SingleTop in AndroidManifest.xml makes sure MainActivity is only
	// launched once.
	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	// Find the address of the marker in the proximity alert notification
	private void handleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		if (Constants.ACTION_FIND_ALERT_MARKER.equals(intent.getAction())) {
			double latitude = extras.getDouble("latitude");
			double longitude = extras.getDouble("longitude");
			GeoPoint geoPoint = new GeoPoint(((int) (latitude * 1E6)),
					((int) (longitude * 1E6)));

			// Zoom in on it
			mapView.getController().animateTo(geoPoint, new Runnable() {

				@Override
				public void run() {
					mapView.getController().setZoom(20);
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		mapView.invalidate();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	private class ReverseGeoCoderTask extends
			AsyncTask<GeoPoint, Void, List<Address>> {
		private GeoPoint geoPoint = null;

		// Runs on the UI thread before doInBackground();
		@Override
		protected void onPreExecute() {
			// Start the progress spinner in a background thread
			Handler handler = new Handler();
			handler.post(new Runnable() {

				public void run() {
					setSupportProgressBarIndeterminateVisibility(true);
				}
			});
		}

		// Runs on a background thread, return is passed to onPostExecute();
		@Override
		protected List<Address> doInBackground(GeoPoint... params) {
			List<Address> addresses = null;
			Geocoder g;
			geoPoint = params[0];

			try {
				g = new Geocoder(MainActivity.this);
				addresses = g.getFromLocation(geoPoint.getLatitudeE6() / 1E6,
						geoPoint.getLongitudeE6() / 1E6, 1);
			} catch (IOException e) {
			}
			return addresses;
		}

		// Runs on the UI thread
		@Override
		protected void onPostExecute(List<Address> addresses) {
			if (addresses != null && addresses.size() > 0) {
				Address a = addresses.get(0);
				int maxAddressLine = a.getMaxAddressLineIndex();
				StringBuffer sb = new StringBuffer("");
				for (int i = 0; i < maxAddressLine; i++) {
					sb.append(a.getAddressLine(i) + " ");
				}

				String address = sb.toString();
				createMarkerDialog(geoPoint, address);
			} else {
				// Let the user know that an address was not found
				Toast.makeText(MainActivity.this,
						R.string.unable_to_find_address, Toast.LENGTH_LONG)
						.show();
			}
			// Stop the progress spinner
			setSupportProgressBarIndeterminateVisibility(false);

		}
	}

	private void createMarkerDialog(final GeoPoint geoPoint,
			final String address) {
		dialog = new AlertDialog.Builder(this)
				.setTitle("Place Marker?")
				.setMessage(address)
				.setPositiveButton("OK", new AlertDialog.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Add the marker to the database
						Position position = new Position();
						position.setAddress(address);
						position.setLatitude(geoPoint.getLatitudeE6() / 1E6);
						position.setLongitude(geoPoint.getLongitudeE6() / 1E6);
						app.addPosition(position);

						// Add a marker to the map
						OverlayItem overlayitem = new OverlayItem(geoPoint,
								"Remove Marker?", address);
						mapView.getOverlays().remove(markersItemizedoverlay);
						markersItemizedoverlay.addOverlay(overlayitem);
						mapView.getOverlays().add(markersItemizedoverlay);
						mapView.invalidate();

						// Create a proximity alert if alerts are active
						boolean proxAlertsActive = preferences.getBoolean(
								"prox_alerts_active", false);
						if (proxAlertsActive) {
							Intent intent = new Intent(
									Constants.ACTION_PROX_ALERT_CRUD_BROADCAST);
							intent.putExtra("create_prox_alert", true);
							intent.putExtra("id", position.getId());
							LocalBroadcastManager
									.getInstance(MainActivity.this)
									.sendBroadcast(intent);
						}

					}
				})
				.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.action_items, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionbar_mylocation:
			// Make sure we avoid the NPE if myLocationOverlay is not displayed
			// yet
			if (myLocationOverlay.getMyLocation() != null) {
				// Zoom in on myLocationOverlay
				mapView.getController().animateTo(
						myLocationOverlay.getMyLocation(), new Runnable() {

							public void run() {
								if (mapView.getZoomLevel() < REASONABLE_ZOOM_LEVEL)
									for (int i = mapView.getZoomLevel(); i < REASONABLE_ZOOM_LEVEL; i++) {
										mapView.getController().zoomIn();
									}
							}
						});
			} else {
				// If the location is not ready, tell the user
				Toast.makeText(this, R.string.waiting_for_location,
						Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.actionbar_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void drawItemizedOverlay() {
		positions = app.getPositions();
		if (positions.size() > 0) {
			if (markersItemizedoverlay != null)
				mapView.getOverlays().remove(markersItemizedoverlay);

			markersItemizedoverlay = new MarkersItemizedOverlay(drawable,
					MainActivity.this);
			// Iterate over all positions (stored in the database)
			// creating a marker for each one, adding it the the itemizedoverlay
			for (Position position : positions) {
				OverlayItem overlayitem = new OverlayItem(new GeoPoint(
						((int) (position.getLatitude() * 1E6)),
						((int) (position.getLongitude() * 1E6))),
						"Remove Marker?", position.getAddress());
				markersItemizedoverlay.addOverlay(overlayitem);
			}

			// Add the itemizedoverlay to the map
			mapView.getOverlays().add(markersItemizedoverlay);
		}
		mapView.invalidate();
	}

}
