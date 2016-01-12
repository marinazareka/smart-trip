package org.fruct.oss.tsp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.fruct.oss.tsp.commondatatype.Schedule;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

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
		db.execSQL(ScheduleTable.queryCreate());
		db.execSQL(SchedulePointsTable.getCreateQuery());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/*@Override
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	public Observable<List<Schedule>> loadSchedules() {
		Cursor cursor = getReadableDatabase().query(ScheduleTable.TABLE,
				ScheduleTable.COLUMN_ALL, null, null, null, null, null, null);

		try {
			ArrayList<Schedule> schedules = new ArrayList<>();
			while (cursor.moveToNext()) {
				schedules.add(ScheduleTable.fromCursor(cursor));
			}
			return schedules;
		} finally {
			cursor.close();
		}
	}*/
}
