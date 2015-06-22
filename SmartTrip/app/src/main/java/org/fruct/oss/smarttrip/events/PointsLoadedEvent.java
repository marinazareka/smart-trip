package org.fruct.oss.smarttrip.events;

import org.fruct.oss.smarttrip.points.Point;

import java.util.List;

public class PointsLoadedEvent {
	private List<Point> points;

	public PointsLoadedEvent(List<Point> pointList) {
		this.points = pointList;
	}

	public List<Point> getPoints() {
		return points;
	}
}
