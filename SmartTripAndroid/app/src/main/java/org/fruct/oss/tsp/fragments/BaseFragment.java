package org.fruct.oss.tsp.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.fruct.oss.tsp.BuildConfig;
import org.fruct.oss.tsp.activities.MainActivity;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;
import org.fruct.oss.tsp.stores.ScheduleStore;
import org.fruct.oss.tsp.viewmodel.GeoViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * @return хранилище геоданных
	 */
	protected GeoStore getGeoStore() {
		return commonFragment.getGeoStore();
	}

	/**
	 * @return хранилище маршрута
	 */
	protected ScheduleStore getScheduleStore() {
		return commonFragment.getScheduleStore();
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

	protected void scheduleSelection(GeoViewModel geoViewModel) {
		List<Point> checkedPoints = new ArrayList<>();
		for (GeoViewModel.PointModel pointModel : geoViewModel.getPoints()) {
			if (pointModel.isChecked) {
				checkedPoints.add(pointModel.point);
			}
		}

		log.debug("{} points searching", checkedPoints.size());

		getSmartSpace().postScheduleRequest(checkedPoints);
	}
}
