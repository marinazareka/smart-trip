package org.fruct.oss.tsp.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.fruct.oss.tsp.BuildConfig;
import org.fruct.oss.tsp.activities.MainActivity;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;

public class BaseFragment extends Fragment {
	private CommonFragment commonFragment;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		setupCommonFragment();
		if (BuildConfig.DEBUG && commonFragment == null)
			throw new AssertionError("CommonFragment must not be null");
	}

	@Override
	public void onDetach() {
		commonFragment = null;
		super.onDetach();
	}

	protected GeoStore getGeoStore() {
		return commonFragment.getGeoStore();
	}

	protected SmartSpace getSmartSpace() {
		return commonFragment.getSmartSpace();
	}

	private void setupCommonFragment() {
		commonFragment = (CommonFragment) getFragmentManager()
				.findFragmentByTag(MainActivity.TAG_COMMON_FRAGMENT);
	}
}
