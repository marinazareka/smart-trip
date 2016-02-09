package org.fruct.oss.tsp.commondatatype;

import org.joda.time.LocalDateTime;

import java.io.Serializable;

/**
 * Сохраненный маршрут
 */
public class Schedule implements Serializable {
	private final long id;
	private final String title;
	private final TspType tspType;

	private final String roadType;
	private final LocalDateTime startDateTime;
	private final LocalDateTime endDateTime;

	public Schedule(String title, TspType tspType, String roadType, LocalDateTime startDateTime, LocalDateTime endDateTime) {
		this(0, title, tspType, roadType, startDateTime, endDateTime);
	}

	public Schedule(long id, String title, TspType tspType, String roadType, LocalDateTime startDateTime, LocalDateTime endDateTime) {
		this.id = id;
		this.title = title;
		this.tspType = tspType;
		this.roadType = roadType;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
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

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

}
