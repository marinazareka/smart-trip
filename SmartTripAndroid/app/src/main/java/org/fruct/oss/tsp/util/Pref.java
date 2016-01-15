package org.fruct.oss.tsp.util;

import android.content.SharedPreferences;

public class Pref {
	public static final String PREF_CURRENT_SCHEDULE = "pref_current_schedule";
	public static final String PREF_USER_ID = "pref_user_id";

	/**
	 * Clears stored current schedule id if expectedId equals current schedule id
	 * @param pref SharedPreferences
	 * @param expectedId expected id
	 */
	public static void compareAndClearCurrentSchedule(SharedPreferences pref, long expectedId) {
		if (getCurrentSchedule(pref) == expectedId) {
			pref.edit().remove(PREF_CURRENT_SCHEDULE).apply();
		}
	}

	public static void setCurrentSchedule(SharedPreferences pref, long scheduleId) {
		pref.edit().putLong(PREF_CURRENT_SCHEDULE, scheduleId).apply();
	}

	public static long getCurrentSchedule(SharedPreferences pref) {
		return pref.getLong(PREF_CURRENT_SCHEDULE, 0);
	}

	// Get user id for using in smartspace
	public static void setUserId(SharedPreferences pref, String userId) {
		pref.edit().putString(PREF_USER_ID, userId).apply();
	}

	public static String getUserId(SharedPreferences preferences) {
		return preferences.getString(PREF_USER_ID, null);
	}
}
