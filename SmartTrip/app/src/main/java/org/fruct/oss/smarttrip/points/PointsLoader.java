package org.fruct.oss.smarttrip.points;

import java.util.List;

/**
 * Interface for synchronous points loading
 */
public interface PointsLoader {
	List<Point> loadPoints(double latCenter, double lonCenter, double radius);
}
