package org.fruct.oss.tsp.util;

import android.content.SharedPreferences;

public class Pref {
	public static final String PREF_CURRENT_SCHEDULE = "pref_current_schedule";

	public static void setCurrentSchedule(SharedPreferences pref, long scheduleId) {
		pref.edit().putLong(PREF_CURRENT_SCHEDULE, scheduleId).apply();
	}

	public static long getCurrentSchedule(SharedPreferences pref) {
		return pref.getLong(PREF_CURRENT_SCHEDULE, 0);
	}
}
