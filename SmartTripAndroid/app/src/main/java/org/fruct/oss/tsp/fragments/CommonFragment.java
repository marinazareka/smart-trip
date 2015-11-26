package org.fruct.oss.tsp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.fruct.oss.tsp.events.LocationEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.smartspace.TestSmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;
import org.fruct.oss.tsp.stores.ScheduleStore;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommonFragment extends Fragment {
	private GeoStore geoStore;
	private ScheduleStore scheduleStore;

	private SmartSpace smartSpace;

	public CommonFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		createGeoStore();
		createScheduleStore();

		createSmartspace();
	}

	private void createScheduleStore() {
		scheduleStore = new ScheduleStore();
	}

	private void createSmartspace() {
		smartSpace = new TestSmartSpace(getActivity());
	}

	private void createGeoStore() {
		geoStore = new GeoStore();
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
		geoStore.start();
		scheduleStore.start();
	}

	@Override
	public void onStop() {
		scheduleStore.stop();
		geoStore.stop();
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	public GeoStore getGeoStore() {
		return geoStore;
	}

	public ScheduleStore getScheduleStore() {
		return scheduleStore;
	}

	public SmartSpace getSmartSpace() {
		return smartSpace;
	}

	// TODO: this method possibly out of place (should be placed somewhere in "LocationStore")
	public void onEventMainThread(LocationEvent locationEvent) {
		smartSpace.updateUserLocation(locationEvent.getLocation());
	}
}
