package org.fruct.oss.tsp.commondatatype;

/**
 * Сохраненный маршрут
 */
public class Schedule {
	private final long id;
	private final String title;
	private final TspType tspType;
	//private final String roadType;
	//private final long intervalStart;
	//private final long intervalEnd;

	public Schedule(String title, TspType tspType) {
		this(0, title, tspType);
	}

	public Schedule(long id, String title, TspType tspType) {
		this.id = id;
		this.title = title;
		this.tspType = tspType;
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
}
