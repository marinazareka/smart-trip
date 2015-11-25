package org.fruct.oss.tsp.viewmodel;

import org.fruct.oss.tsp.data.Point;

import java.util.List;

public interface GeoViewModel {
	void registerListener(Listener listener);
	void unregisterListener(Listener listener);

	void start();
	void stop();

	/**
	 * Get local cached points
	 * @return list of points
	 */
	List<PointModel> getPoints();

	boolean isAnythingChecked();

	void setCheckedState(int position, boolean checked);

	interface Listener {
		void pointsUpdated(List<PointModel> points);
	}

	class PointModel {
		public boolean isChecked;
		public Point point;
	}
}
