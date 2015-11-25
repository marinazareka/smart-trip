package org.fruct.oss.tsp.smartspace;

import android.location.Location;

import org.fruct.oss.tsp.data.Point;
import org.fruct.oss.tsp.data.ScheduleRequest;
import org.fruct.oss.tsp.data.SearchRequest;
import org.fruct.oss.tsp.data.User;

import java.util.List;

public interface SmartSpace {
	void publishUser(User user);
	void updateUserLocation(Location location);

	SearchRequest postSearchRequest(double radius, String pattern);
	ScheduleRequest postScheduleRequest(List<Point> pointList);
}
