package org.fruct.oss.tsp.fragments.root;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.util.UserPref;

public class PrefFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
	private SharedPreferences pref;

	@Override
	public void onCreatePreferences(Bundle bundle, String rootKey) {
		addPreferencesFromResource(R.xml.preferences);
		pref = getPreferenceManager().getSharedPreferences();
	}

	@Override
	public void onResume() {
		super.onResume();
		pref.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(pref, UserPref.PREF_RADIUS);
		onSharedPreferenceChanged(pref, UserPref.PREF_SIB_ADDRESS);
		onSharedPreferenceChanged(pref, UserPref.PREF_SIB_PORT);
	}

	@Override
	public void onPause() {
		pref.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	private void setupRadiusPreference(SharedPreferences pref, int radius) {
		Preference radiusPreference = findPreference(UserPref.PREF_RADIUS);
		radiusPreference.setSummary(getResources().getQuantityString(R.plurals.pref_radius_summary, radius, radius));
	}

	private void setupSibAddressPreference(SharedPreferences pref, String address) {
		Preference sibAddressPreference = findPreference(UserPref.PREF_SIB_ADDRESS);
		sibAddressPreference.setSummary(address);
	}

	private void setupSibPortPreference(SharedPreferences pref, int port) {
		Preference sibAddressPreference = findPreference(UserPref.PREF_SIB_PORT);
		sibAddressPreference.setSummary(String.valueOf(port));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		switch (key) {
		case UserPref.PREF_RADIUS:
			setupRadiusPreference(pref, UserPref.getRadius(pref));
			break;

		case UserPref.PREF_SIB_ADDRESS:
			setupSibAddressPreference(pref, UserPref.getSibAddress(pref));
			break;

		case UserPref.PREF_SIB_PORT:
			setupSibPortPreference(pref, UserPref.getSibPort(pref));
			break;
		}
	}
}
