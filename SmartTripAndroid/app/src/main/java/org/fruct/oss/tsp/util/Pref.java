package org.fruct.oss.tsp.util;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.fruct.oss.tsp.fragments.root.MapFragment;

public class Pref {
	public static final String PREF_CURRENT_SCHEDULE = "pref_current_schedule";
	public static final String PREF_USER_ID = "pref_user_id";

	public static final String PREF_MAP_STATE = "pref_map_state_lat";

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

	public static boolean hasCurrentSchedule(SharedPreferences pref) {
		return pref.getLong(PREF_CURRENT_SCHEDULE, -1) != -1;
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

	public static void setMapState(SharedPreferences pref, MapFragment.MapState state) {
		pref.edit()
				.putFloat(PREF_MAP_STATE + "lat", (float) state.lat)
				.putFloat(PREF_MAP_STATE + "lon", (float) state.lon)
				.putInt(PREF_MAP_STATE + "zoom", state.zoom)
				.putBoolean(PREF_MAP_STATE + "isFollowing", state.isFollowing)
				.apply();
	}

	@NonNull
	public static MapFragment.MapState getMapState(SharedPreferences pref) {
		return new MapFragment.MapState(
				pref.getFloat(PREF_MAP_STATE + "lat", (float) (30 + Math.random() * 30)),
				pref.getFloat(PREF_MAP_STATE + "lon", (float) (30 + Math.random() * 30)),
				pref.getInt(PREF_MAP_STATE + "zoom", 15),
				pref.getBoolean(PREF_MAP_STATE + "isFollowing", true));
	}
}
