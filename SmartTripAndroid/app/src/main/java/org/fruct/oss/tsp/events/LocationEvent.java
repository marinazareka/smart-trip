package org.fruct.oss.tsp.events;

import android.location.Location;

/**
 * Событие обновление координат пользователя
 */
public class LocationEvent {
	private Location location;

	public LocationEvent(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}
}
