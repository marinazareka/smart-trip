package org.fruct.oss.tsp.stores;

import android.support.annotation.NonNull;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.events.GeoStoreChangedEvent;
import org.fruct.oss.tsp.events.SearchEvent;
import org.fruct.oss.tsp.events.SearchStoreChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Локальное хранилище данных о загруженных географических объектах.
 *
 * Подписывается на событие обновления и обновляет свое состояние при получении события.
 */
public class SearchStore implements Store {
	private static final Logger log = LoggerFactory.getLogger(SearchStore.class);

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
		return Collections.unmodifiableList(points);
	}

	public void onEventMainThread(SearchEvent searchEvent) {
		log.debug("Search store updated");
		points = searchEvent.getPoints();
		EventBus.getDefault().post(new SearchStoreChangedEvent());
	}
}
