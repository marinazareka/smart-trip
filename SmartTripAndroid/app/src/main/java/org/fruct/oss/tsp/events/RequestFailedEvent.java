package org.fruct.oss.tsp.events;

public class RequestFailedEvent {
	private String description;

	public RequestFailedEvent(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
