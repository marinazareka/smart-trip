package org.fruct.oss.tsp.viewmodel;

import org.fruct.oss.tsp.BuildConfig;
import org.fruct.oss.tsp.commondatatypes.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractGeoViewModel implements GeoViewModel {
	private CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();

	private List<PointModel> shownPoints = new ArrayList<>();
	private int checkedCount;

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
	public boolean isAnythingChecked() {
		if (BuildConfig.DEBUG && !(checkedCount >= 0)) throw new AssertionError();
		return checkedCount > 0;
	}

	@Override
	public void setCheckedState(int position, boolean checked) {
		PointModel pointModel = shownPoints.get(position);

		if (pointModel.isChecked != checked) {
			if (checked) {
				checkedCount++;
			} else {
				checkedCount--;
			}
		}

		pointModel.isChecked = checked;
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

		checkedCount = existingPointsChecked.size();
		notifyPointsUpdated(shownPoints);
	}

	protected void notifyPointsUpdated(List<PointModel> points) {
		for (Listener listener : listeners) {
			listener.pointsUpdated(points);
		}
	}
}
