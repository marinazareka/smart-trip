package org.fruct.oss.tsp.stores;

import android.util.Log;

import org.fruct.oss.tsp.data.Movement;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.fruct.oss.tsp.events.ScheduleStoreChangedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Локальное хранилище данных о маршруте.
 *
 * Подписывается на событие обновления и обновляет свое состояние при получении события.
 */
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

	/**
	 * Получить текущие отрезки маршрута.
	 * @return список текущих отрезков маршрута. Может быть немодифицируемым.
	 */
	public List<Movement> getCurrentSchedule() {
		return currentSchedule;
	}
}
