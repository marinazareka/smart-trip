package org.fruct.oss.tsp.database;

import java.util.Locale;

public class ScheduleTable {
	public static final String TABLE = "schedule";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TSP_TYPE = "tsptype";

	public static String getCreateQuery() {
		return String.format(Locale.US,
				"CREATE TABLE %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT NOT NULL, " +
						"%s TEXT NOT NULL" +
						");",
				TABLE, COLUMN_ID, COLUMN_TITLE, COLUMN_TSP_TYPE );
	}
}
