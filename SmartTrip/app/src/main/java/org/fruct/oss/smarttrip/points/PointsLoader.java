package org.fruct.oss.smarttrip.points;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Interface for synchronous points loading
 */
public interface PointsLoader {
	List<Point> loadPoints(double latCenter, double lonCenter, double radius, @Nullable String pattern);
}
