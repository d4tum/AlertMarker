package com.example.alertmarker;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AlertMarkerApplication extends Application {
	private ArrayList<Position> currentPosistions;
	private SQLiteDatabase database;
	private String[] allColumns = { PositionsSQLiteOpenHelper.POSITION_ID,
			PositionsSQLiteOpenHelper.POSITION_ADDRESS,
			PositionsSQLiteOpenHelper.POSITION_LATITUDE,
			PositionsSQLiteOpenHelper.POSITION_LONGITUDE };

	@Override
	public void onCreate() {
		super.onCreate();
		PositionsSQLiteOpenHelper helper = new PositionsSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
		if (currentPosistions == null) {
			currentPosistions = loadPositions();
		}
	}

	@Override
	public void onTerminate() {
		database.close();
		super.onTerminate();
	}

	public ArrayList<Position> loadPositions() {
		ArrayList<Position> positions = new ArrayList<Position>();

		Cursor cursor = database.query(
				PositionsSQLiteOpenHelper.POSITIONS_TABLE, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		Position p;
		if (!cursor.isAfterLast()) {
			do {
				p = new Position();
				p.setId(cursor.getInt(0));
				p.setAddress(cursor.getString(1));
				p.setLatitude(cursor.getFloat(2));
				p.setLongitude(cursor.getFloat(3));
				positions.add(p);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return positions;
	}

	public ArrayList<Position> getPositions() {
		return currentPosistions;
	}

	public void addPosition(Position position) {
		assert (null != position);

		ContentValues values = new ContentValues();
		values.put(PositionsSQLiteOpenHelper.POSITION_ADDRESS,
				position.getAddress());
		values.put(PositionsSQLiteOpenHelper.POSITION_LATITUDE,
				position.getLatitude());
		values.put(PositionsSQLiteOpenHelper.POSITION_LONGITUDE,
				position.getLongitude());
		position.setId(database.insert(
				PositionsSQLiteOpenHelper.POSITIONS_TABLE, null, values));
		currentPosistions.add(position);
	}

	public void deletePosition(Position position) {
		long id = position.getId();
		database.delete(PositionsSQLiteOpenHelper.POSITIONS_TABLE,
				PositionsSQLiteOpenHelper.POSITION_ID + " = " + id, null);
		currentPosistions.remove(position);
	}
}
