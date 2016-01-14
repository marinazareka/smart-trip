package org.fruct.oss.tsp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import org.fruct.oss.tsp.App;
import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.events.RequestFailedEvent;
import org.fruct.oss.tsp.smartspace.BoundSmartSpace;
import org.fruct.oss.tsp.smartspace.ScheduleUpdater;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;
import org.fruct.oss.tsp.stores.ScheduleStore;
import org.fruct.oss.tsp.stores.SearchStore;
import org.fruct.oss.tsp.util.LocationProvider;

import de.greenrobot.event.EventBus;

/**
 * Фрагмент без пользовательского интерфейса, предназначен для хранения глобальных объектов приложения,
 * сохраняя их состояния при изменении конфигурации устройства (например поворот экрана).
 */
public class CommonFragment extends Fragment {
	private GeoStore geoStore;
	private ScheduleStore scheduleStore;
	private SearchStore searchStore;

	private BoundSmartSpace smartSpace;

	private ScheduleUpdater scheduleUpdater;

	public CommonFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		createGeoStore();
		createScheduleStore();
		createSearchStore();

		createSmartspace();

		createScheduleUpdater();
	}

	private void createScheduleUpdater() {
		scheduleUpdater = new ScheduleUpdater(getContext(),
				LocationProvider.getObservable(getContext(), LocationProvider.REQUEST_SMARTSPACE),
				App.getInstance().getDatabase(), getSmartSpace());
	}

	private void createScheduleStore() {
		scheduleStore = new ScheduleStore();
	}

	private void createSmartspace() {
		smartSpace = new BoundSmartSpace(getActivity());
	}

	private void createGeoStore() {
		geoStore = new GeoStore();
	}

	private void createSearchStore() {
		searchStore = new SearchStore();
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
		geoStore.start();
		scheduleStore.start();
		smartSpace.start();
		searchStore.start();
		scheduleUpdater.start();
	}

	@Override
	public void onStop() {
		scheduleUpdater.stop();
		searchStore.stop();
		smartSpace.stop();
		scheduleStore.stop();
		geoStore.stop();
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	/**
	 * @return хранилище геоданных
	 */
	public GeoStore getGeoStore() {
		return geoStore;
	}

	/**
	 * @return хранилище маршрута
	 */
	public ScheduleStore getScheduleStore() {
		return scheduleStore;
	}

	/**
	 * @return хранилище результатов поиска
	 */
	public SearchStore getSearchStore() {
		return searchStore;
	}

	/**
	 * @return интерфейс интеллектуального пространства
	 */
	public SmartSpace getSmartSpace() {
		return smartSpace;
	}


	public void onEventMainThread(RequestFailedEvent event) {
		Toast.makeText(getContext(), R.string.str_request_failed, Toast.LENGTH_SHORT).show();
		Toast.makeText(getContext(), event.getDescription(), Toast.LENGTH_LONG).show();
	}
}
