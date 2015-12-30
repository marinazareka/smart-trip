package org.fruct.oss.tsp.database;

import java.util.Locale;

public class SchedulePointsTable {
	public static final String TABLE = "schedulePoints";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_POINT_ID = "pointId";
	public static final String COLUMN_SCHEDULE_ID = "scheduleId";

	public static String getCreateQuery() {
		return String.format(Locale.US,
				"CREATE TABLE %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s INTEGER NOT NULL, " +
						"%s INTEGER NOT NULL, " +
						"FOREIGN KEY (%s) REFERENCES %s (%s)," +
						"FOREIGN KEY (%s) REFERENCES %s (%s)" +
						");",
				TABLE, COLUMN_ID, COLUMN_POINT_ID, COLUMN_SCHEDULE_ID,
				COLUMN_POINT_ID, PointsTable.TABLE, PointsTable.COLUMN_ID,
				COLUMN_SCHEDULE_ID, ScheduleTable.TABLE, ScheduleTable.COLUMN_ID );
	}
}
