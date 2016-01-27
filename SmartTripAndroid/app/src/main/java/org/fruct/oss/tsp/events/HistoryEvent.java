package org.fruct.oss.tsp.events;

import java.util.List;

public class HistoryEvent {
	private List<String> patterns;

	public HistoryEvent(List<String> patterns) {
		this.patterns = patterns;
	}

	public List<String> getPatterns() {
		return patterns;
	}
}
