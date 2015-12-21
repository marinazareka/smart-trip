package org.fruct.oss.tsp.util;

import android.content.SharedPreferences;

public class Pref {
	public static final String PREF_RADIUS = "pref_radius";
	public static final String PREF_RADIUS_DEFAULT = "10000";

	public static final String PREF_CLOSED = "pref_closed";
	public static final boolean PREF_CLOSED_DEFAULT = true;

	public static int getRadius(SharedPreferences pref) {
		try {
			return Integer.parseInt(pref.getString(PREF_RADIUS, PREF_RADIUS_DEFAULT));
		} catch (NumberFormatException ex) {
			return Integer.parseInt(PREF_RADIUS_DEFAULT);
		}
	}

	public static boolean isClosedRoute(SharedPreferences preferences) {
		return preferences.getBoolean(PREF_CLOSED, PREF_CLOSED_DEFAULT);
	}
}
