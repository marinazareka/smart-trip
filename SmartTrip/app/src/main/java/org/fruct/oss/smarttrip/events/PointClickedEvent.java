package org.fruct.oss.smarttrip.events;

import org.fruct.oss.smarttrip.points.Point;

public class PointClickedEvent {
	private Point point;

	public PointClickedEvent(Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}
}
