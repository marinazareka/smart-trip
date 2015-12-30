package org.fruct.oss.tsp.database;

import java.util.Locale;

public class PointsTable {
	public static final String TABLE = "points";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_REMOTE_ID = "remoteId";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LON = "lon";

	public static String getCreateQuery() {
		return String.format(Locale.US,
				"CREATE TABLE %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT NULL, " +
						"%s TEXT NULL, " +
						"%s REAL NOT NULL, " +
						"%s REAL NOT NULL" +
						");",
				TABLE, COLUMN_ID, COLUMN_REMOTE_ID, COLUMN_TITLE, COLUMN_LAT, COLUMN_LON);
	}
}
