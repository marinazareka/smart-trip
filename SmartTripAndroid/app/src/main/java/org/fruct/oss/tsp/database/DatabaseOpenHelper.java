package org.fruct.oss.tsp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;

	public DatabaseOpenHelper(Context context) {
		super(context, "smarttripdb", null, VERSION);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys = 1;");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PointsTable.getCreateQuery());
		db.execSQL(ScheduleTable.getCreateQuery());
		db.execSQL(SchedulePointsTable.getCreateQuery());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
