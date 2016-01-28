package org.fruct.oss.tsp.commondatatype;

import org.joda.time.LocalDate;

/**
 * Сохраненный маршрут
 */
public class Schedule {
	private final long id;
	private final String title;
	private final TspType tspType;

	private final String roadType;
	private final LocalDate startDate;
	private final LocalDate endDate;

	public Schedule(String title, TspType tspType, String roadType, LocalDate startDate, LocalDate endDate) {
		this(0, title, tspType, roadType, startDate, endDate);
	}

	public Schedule(long id, String title, TspType tspType, String roadType, LocalDate startDate, LocalDate endDate) {
		this.id = id;
		this.title = title;
		this.tspType = tspType;
		this.roadType = roadType;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public TspType getTspType() {
		return tspType;
	}

	public String getRoadType() {
		return roadType;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}
}
