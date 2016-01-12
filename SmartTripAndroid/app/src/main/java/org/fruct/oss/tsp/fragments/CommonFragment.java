package org.fruct.oss.tsp.fragments;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import org.fruct.oss.tsp.App;
import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.events.LocationEvent;
import org.fruct.oss.tsp.events.RequestFailedEvent;
import org.fruct.oss.tsp.smartspace.BoundSmartSpace;
import org.fruct.oss.tsp.smartspace.ScheduleUpdater;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;
import org.fruct.oss.tsp.stores.ScheduleStore;
import org.fruct.oss.tsp.stores.SearchStore;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

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

	private Subject<Location, Location> locationObservable
			= new SerializedSubject<>(PublishSubject.<Location>create());

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
				locationObservable,
				App.getInstance().getDatabase(),
				getSmartSpace());
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

	// TODO: this method possibly out of place (should be placed somewhere in "LocationStore")
	public void onEventMainThread(LocationEvent locationEvent) {
		//smartSpace.updateUserLocation(locationEvent.getLocation());
		locationObservable.onNext(locationEvent.getLocation());
	}

	public void onEventMainThread(RequestFailedEvent event) {
		Toast.makeText(getContext(), R.string.str_request_failed, Toast.LENGTH_SHORT).show();
		Toast.makeText(getContext(), event.getDescription(), Toast.LENGTH_LONG).show();
	}
}
