package org.fruct.oss.tsp.database.tables;

import java.util.Locale;

public class CurrentScheduleTable {
	public static final String TABLE = "currentSchedule";

	public static final String COLUMN_SCHEDULE_ID = "scheduleId";

	public static String getCreateQuery() {
		return String.format(Locale.US,
				"CREATE TABLE %s (" +
						"%s INTEGER NULL," +

						"FOREIGN KEY (%s) REFERENCES %s (%s)" +
						");",
				TABLE, COLUMN_SCHEDULE_ID,
				COLUMN_SCHEDULE_ID, ScheduleTable.TABLE, ScheduleTable.COLUMN_ID);
	}

	public static String getInitializeQuery() {
		return String.format(Locale.US,
				"INSERT INTO %s VALUES (%s);",
				TABLE, 0);
	}

}
