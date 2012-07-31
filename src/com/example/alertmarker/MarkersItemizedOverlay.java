package com.example.alertmarker;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MarkersItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Context context;
	private AlertDialog dialog;
	private AlertMarkerApplication app;
	private SharedPreferences preferences;

	public MarkersItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));

	}

	public MarkersItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
		app = (AlertMarkerApplication) context.getApplicationContext();
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
		populate();
	}

	@Override
	public int size() {
		return overlays.size();
	}

	@Override
	protected boolean onTap(final int index) {
		final OverlayItem item = overlays.get(index);
		dialog = new AlertDialog.Builder(context)
				.setTitle(item.getTitle())
				.setMessage(item.getSnippet())
				.setPositiveButton("OK", new AlertDialog.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						String address = item.getSnippet();
						Position removePosition = new Position();

						for (Position position : app.getPositions()) {
							if (position.getAddress().equals(address))
								removePosition = position;
						}

						boolean proxAlertsActive = preferences.getBoolean(
								"prox_alerts_active", false);

						if (proxAlertsActive) {
							Intent intent = new Intent(
									Constants.ACTION_PROX_ALERT_CRUD_BROADCAST);
							intent.putExtra("delete_prox_alert", true);

							intent.putExtra("id", removePosition.getId());
							LocalBroadcastManager.getInstance(context)
									.sendBroadcast(intent);
						}

						app.deletePosition(removePosition);
						overlays.remove(index);
						((MainActivity) context).drawItemizedOverlay();
					}
				})
				.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();
		dialog.show();
		return true;
	}

}