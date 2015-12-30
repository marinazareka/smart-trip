package org.fruct.oss.tsp.commondatatype;

/**
 * Сохраненный маршрут
 */
public class Schedule {
	private String title;
	private TspType tspType;

	public Schedule(String title, TspType tspType) {
		this.title = title;
		this.tspType = tspType;
	}

	public String getTitle() {
		return title;
	}

	public TspType getTspType() {
		return tspType;
	}
}
