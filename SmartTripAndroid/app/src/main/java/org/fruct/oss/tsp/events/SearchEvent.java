package org.fruct.oss.tsp.events;

import org.fruct.oss.tsp.data.Point;

import java.util.List;

public class SearchEvent {
	private List<Point> points;

	public SearchEvent(List<Point> points) {
		this.points = points;
	}

	public List<Point> getPoints() {
		return points;
	}
}
