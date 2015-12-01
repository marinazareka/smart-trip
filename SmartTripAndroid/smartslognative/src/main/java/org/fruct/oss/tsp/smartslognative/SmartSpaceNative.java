package org.fruct.oss.tsp.smartslognative;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;

public interface SmartSpaceNative {
	void publishUser(String userId);

	void updateUserLocation(double lat, double lon);

	void postSearchRequest(double radius, String pattern);

	void postScheduleRequest(Point[] points);

	// Callbacks
	void onSearchRequestReady(Point[] points);
	void onScheduleRequestReady(Movement[] movements);
}