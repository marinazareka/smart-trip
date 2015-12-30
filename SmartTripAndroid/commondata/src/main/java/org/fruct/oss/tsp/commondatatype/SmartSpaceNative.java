package org.fruct.oss.tsp.commondatatype;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;

import java.io.IOException;

public interface SmartSpaceNative {
	void initialize(String userId, String kpName, String smartSpaceName, String address, int port) throws IOException;

	void shutdown();

	void updateUserLocation(double lat, double lon);

	void postSearchRequest(double radius, String pattern);

	void postScheduleRequest(Point[] points, String name);

	void setListener(Listener listener);

	interface Listener {
		// Callbacks
		void onSearchRequestReady(Point[] points);
		void onScheduleRequestReady(Movement[] movements);
		void onRequestFailed(String description);
	}
}