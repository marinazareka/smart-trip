package org.fruct.oss.tsp.smartspace;

import android.location.Location;

import org.fruct.oss.tsp.data.SearchRequest;
import org.fruct.oss.tsp.data.User;

public interface SmartSpace {
	void publishUser(User user);
	void updateUserLocation(Location location);

	SearchRequest postRequest(double radius, String pattern);
}
