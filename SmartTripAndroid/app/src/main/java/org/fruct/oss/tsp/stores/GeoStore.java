package org.fruct.oss.tsp.stores;

import org.fruct.oss.tsp.data.Point;
import org.fruct.oss.tsp.events.GeoStoreChangedEvent;
import org.fruct.oss.tsp.events.SearchEvent;

import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class GeoStore implements Store {
	private List<Point> points = Collections.emptyList();

	@Override
	public void start() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public List<Point> getPoints() {
		return points;
	}

	public void onEventMainThread(SearchEvent searchEvent) {
		points = searchEvent.getPoints();
		EventBus.getDefault().post(new GeoStoreChangedEvent());
	}
}
