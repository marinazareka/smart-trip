package org.fruct.oss.tsp.events;

public class HistoryAppendEvent {
	private final String pattern;

	public HistoryAppendEvent(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}
}
