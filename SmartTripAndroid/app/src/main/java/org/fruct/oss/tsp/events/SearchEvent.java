package org.fruct.oss.tsp.events;

import org.fruct.oss.tsp.commondatatypes.Point;

import java.util.List;

/**
 * Уведомлении об выполнении запроса географических объектов
 */
public class SearchEvent {
	private List<Point> points;

	public SearchEvent(List<Point> points) {
		this.points = points;
	}

	public List<Point> getPoints() {
		return points;
	}
}
