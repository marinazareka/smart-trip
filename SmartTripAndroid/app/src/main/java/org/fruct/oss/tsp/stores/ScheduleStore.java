package org.fruct.oss.tsp.stores;

import android.util.Log;

import org.fruct.oss.tsp.data.Movement;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.fruct.oss.tsp.events.ScheduleStoreChangedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ScheduleStore implements Store {
	private static final String TAG = "ScheduleStore";

	private List<Movement> currentSchedule = new ArrayList<>();

	@Override
	public void start() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(ScheduleEvent event) {
		currentSchedule.clear();
		currentSchedule.addAll(event.getMovements());

		Log.d(TAG, "Schedule store updated");
		EventBus.getDefault().post(new ScheduleStoreChangedEvent());
	}

	public List<Movement> getCurrentSchedule() {
		return currentSchedule;
	}
}
