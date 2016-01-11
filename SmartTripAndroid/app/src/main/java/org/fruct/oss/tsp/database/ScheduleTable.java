package org.fruct.oss.tsp.database;

import android.content.ContentValues;
import android.database.Cursor;

import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;

import java.util.Locale;

public class ScheduleTable {
	public static final String TABLE = "schedule";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TSP_TYPE = "tsptype";

	public static final String[] COLUMN_ALL = {COLUMN_ID, COLUMN_TITLE, COLUMN_TSP_TYPE};

	public static String getCreateQuery() {
		return String.format(Locale.US,
				"CREATE TABLE %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT NOT NULL, " +
						"%s TEXT NOT NULL" +
						");",
				TABLE, COLUMN_ID, COLUMN_TITLE, COLUMN_TSP_TYPE );
	}

	public static Schedule fromCursor(Cursor cursor) {
		return new Schedule(cursor.getString(1), TspType.valueOf(cursor.getString(2)));
	}

	public static ContentValues toContentValues(Schedule schedule) {
		ContentValues cv = new ContentValues(2);
		cv.put(COLUMN_TITLE, schedule.getTitle());
		cv.put(COLUMN_TSP_TYPE, schedule.getTspType().name());
		return cv;
	}
}
