package org.fruct.oss.tsp.stores;

import android.support.annotation.NonNull;
import android.util.Log;

import org.fruct.oss.tsp.data.Point;
import org.fruct.oss.tsp.events.GeoStoreChangedEvent;
import org.fruct.oss.tsp.events.SearchEvent;

import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Локальное хранилище данных о загруженных географических объектах.
 *
 * Подписывается на событие обновления и обновляет свое состояние при получении события.
 */
public class GeoStore implements Store {
	private static final String TAG = "GeoStore";

	private List<Point> points = Collections.emptyList();

	@Override
	public void start() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	/**
	 * Получить текущие объекты.
	 * @return список текущих объектов. Может быть немодифицируемым.
	 */
	@NonNull
	public List<Point> getPoints() {
		return points;
	}

	public void onEventMainThread(SearchEvent searchEvent) {
		points = searchEvent.getPoints();

		Log.d(TAG, "Schedule store updated");
		EventBus.getDefault().post(new GeoStoreChangedEvent());
	}
}
