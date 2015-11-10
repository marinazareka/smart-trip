package org.fruct.oss.tsp.model;

import java.util.List;

public interface TripModel {
	void registerListener(Listener listener);
	void unregisterListener(Listener listener);

	/**
	 * Get local cached points
	 * @return list of points
	 */
	List<Point> getPoints();

	interface Listener {
		void pointsUpdated(List<Point> points);
	}
}
