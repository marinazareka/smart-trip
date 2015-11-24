package org.fruct.oss.tsp.model;

import java.util.List;

public interface TripModel {
	void registerListener(Listener listener);
	void unregisterListener(Listener listener);

	void start();
	void stop();

	/**
	 * Get local cached points
	 * @return list of points
	 */
	List<PointModel> getPoints();

	void setCheckedState(int position, boolean checked);

	interface Listener {
		void pointsUpdated(List<PointModel> points);
	}

	public class PointModel {
		public boolean isChecked;
		public Point point;
	}
}
