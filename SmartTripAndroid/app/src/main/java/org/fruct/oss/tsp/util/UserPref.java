package org.fruct.oss.tsp.util;

import android.content.SharedPreferences;


public class UserPref {
	public static final String PREF_RADIUS = "pref_radius";
	public static final String PREF_RADIUS_DEFAULT = "10000";

	public static final String PREF_CLOSED = "pref_closed";
	public static final boolean PREF_CLOSED_DEFAULT = true;

	public static final String PREF_SIB_ADDRESS = "pref_sib_address";
	public static final String PREF_SIB_ADDRESS_DEFAULT = "etourism.cs.karelia.ru";

	public static final String PREF_SIB_PORT = "pref_sib_port";
	public static final String PREF_SIB_PORT_DEFAULT = "20203";

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

	public static String getSibAddress(SharedPreferences preferences) {
		return preferences.getString(PREF_SIB_ADDRESS, PREF_SIB_ADDRESS_DEFAULT);
	}

	public static int getSibPort(SharedPreferences preferences) {
		try {
			return Integer.parseInt(preferences.getString(PREF_SIB_PORT, PREF_SIB_PORT_DEFAULT));
		} catch (NumberFormatException ex) {
			return Integer.parseInt(PREF_SIB_PORT_DEFAULT);
		}
	}
}
