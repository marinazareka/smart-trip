package org.fruct.oss.tsp.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.fruct.oss.tsp.App;
import org.fruct.oss.tsp.BuildConfig;
import org.fruct.oss.tsp.activities.MainActivity;
import org.fruct.oss.tsp.database.DatabaseRepo;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;
import org.fruct.oss.tsp.stores.ScheduleStore;
import org.fruct.oss.tsp.stores.SearchStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Базовый фрагмент, предназначенный для получения общих зависимостей для фрагментов.
 */
public class BaseFragment extends Fragment {
	private static final Logger log = LoggerFactory.getLogger(BaseFragment.class);

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

	protected DatabaseRepo getDatabase() {
		return App.getInstance().getDatabase();
	}


	/**
	 * @return хранилище маршрута
	 */
	protected ScheduleStore getScheduleStore() {
		return commonFragment.getScheduleStore();
	}

	/**
	 * @return хранилище результатов поиска
	 */
	protected SearchStore getSearchStore() {
		return commonFragment.getSearchStore();
	}


	/**
	 * @return интерфейс интеллектуального пространства
	 */
	protected SmartSpace getSmartSpace() {
		return commonFragment.getSmartSpace();
	}

	private void setupCommonFragment() {
		commonFragment = (CommonFragment) getFragmentManager()
				.findFragmentByTag(MainActivity.TAG_COMMON_FRAGMENT);
	}

}
