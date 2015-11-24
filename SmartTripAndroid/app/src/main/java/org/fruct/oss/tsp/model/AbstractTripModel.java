package org.fruct.oss.tsp.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractTripModel implements TripModel {
	private CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();

	private List<PointModel> shownPoints = new ArrayList<>();

	@Override
	public void registerListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void unregisterListener(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public List<PointModel> getPoints() {
		return new ArrayList<>(shownPoints);
	}

	@Override
	public void setCheckedState(int position, boolean checked) {
		shownPoints.get(position).isChecked = checked;
	}

	protected void updatePoints(List<Point> newPoints) {
		HashSet<String> existingPointsChecked = new HashSet<>();
		for (PointModel shownPoint : shownPoints) {
			if (shownPoint.isChecked) {
				existingPointsChecked.add(shownPoint.point.getId());
			}
		}

		shownPoints.clear();
		for (Point newPoint : newPoints) {
			PointModel pointModel = new PointModel();
			pointModel.point = newPoint;
			pointModel.isChecked = existingPointsChecked.contains(newPoint.getId());
			shownPoints.add(pointModel);
		}

		notifyPointsUpdated(shownPoints);
	}

	protected void notifyPointsUpdated(List<PointModel> points) {
		for (Listener listener : listeners) {
			listener.pointsUpdated(points);
		}
	}
}
