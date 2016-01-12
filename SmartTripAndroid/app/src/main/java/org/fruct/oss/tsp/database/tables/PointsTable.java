package org.fruct.oss.tsp.database.tables;

import android.content.ContentValues;
import android.database.Cursor;

import org.fruct.oss.tsp.commondatatype.Point;

import java.util.Locale;

import rx.functions.Func1;

public class PointsTable {
	public static final String TABLE = "points";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_REMOTE_ID = "remoteId";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LON = "lon";
	public static final String COLUMN_SCHEDULE_ID = "scheduleId";

	public static final Func1<Cursor, Point> MAPPER = new Func1<Cursor, Point>() {
		@Override
		public Point call(Cursor cursor) {
			return fromCursor(cursor);
		}
	};

	public static String getCreateQuery() {
		return String.format(Locale.US,
				"CREATE TABLE %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT NULL, " +
						"%s TEXT NULL, " +
						"%s REAL NOT NULL, " +
						"%s REAL NOT NULL," +
						"%s INTEGER NOT NULL," +

						"FOREIGN KEY (%s) REFERENCES %s (%s)" +
						");",
				TABLE, COLUMN_ID, COLUMN_REMOTE_ID, COLUMN_TITLE, COLUMN_LAT, COLUMN_LON, COLUMN_SCHEDULE_ID,
				COLUMN_SCHEDULE_ID, ScheduleTable.TABLE, ScheduleTable.COLUMN_ID );
	}

	public static Point fromCursor(Cursor cursor) {
		return new Point(
				cursor.getString(1),
				cursor.getString(2),
				cursor.getDouble(3),
				cursor.getDouble(4)
		);
	}

	public static ContentValues toContentValues(Point point, long scheduleId) {
		ContentValues cv = new ContentValues(5);
		cv.put(COLUMN_REMOTE_ID, point.getId());
		cv.put(COLUMN_TITLE, point.getTitle());
		cv.put(COLUMN_LAT, point.getLat());
		cv.put(COLUMN_LON, point.getLon());
		cv.put(COLUMN_SCHEDULE_ID, scheduleId);
		return cv;
	}
}
