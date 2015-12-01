package org.fruct.oss.tsp.events;

import org.fruct.oss.tsp.commondatatype.Movement;

import java.util.List;

/**
 * Уведомлении об выполнении запроса построения маршрута
 */
public class ScheduleEvent {
	private List<Movement> movements;

	public ScheduleEvent(List<Movement> movements) {
		this.movements = movements;
	}

	public List<Movement> getMovements() {
		return movements;
	}
}
