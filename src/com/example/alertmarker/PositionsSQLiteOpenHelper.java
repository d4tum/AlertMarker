package com.example.alertmarker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PositionsSQLiteOpenHelper extends SQLiteOpenHelper {
	public static final int VERSION = 2;
	public static final String DB_NAME = "positions_db.sqlite";
	public static final String POSITIONS_TABLE = "positions";
	public static final String POSITION_ID = "id";
	public static final String POSITION_ADDRESS = "address";
	public static final String POSITION_LATITUDE = "latitude";
	public static final String POSITION_LONGITUDE = "longitude";

	public PositionsSQLiteOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		dropAndCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("alter table " + POSITIONS_TABLE + " add column "
				+ POSITION_ADDRESS + " text");
		db.execSQL("alter table " + POSITIONS_TABLE + " add column "
				+ POSITION_LATITUDE + " integer");
		db.execSQL("alter table " + POSITIONS_TABLE + " add column "
				+ POSITION_LONGITUDE + " integer");
	}

	protected void dropAndCreate(SQLiteDatabase db) {
		db.execSQL("drop table if exists " + POSITIONS_TABLE + ";");
		createTables(db);
	}

	protected void createTables(SQLiteDatabase db) {
		db.execSQL("create table " + POSITIONS_TABLE + " (" + POSITION_ID
				+ " integer primary key autoincrement not null,"
				+ POSITION_ADDRESS + " text, " + POSITION_LATITUDE
				+ " integer, " + POSITION_LONGITUDE + " integer " + ");");
	}

}
