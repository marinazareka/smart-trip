package org.fruct.oss.tsp.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractTripModel implements TripModel {
	private CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();

	@Override
	public void registerListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void unregisterListener(Listener listener) {
		listeners.remove(listener);
	}

	protected void notifyPointsUpdated(List<Point> points) {
		for (Listener listener : listeners) {
			listener.pointsUpdated(points);
		}
	}
}
