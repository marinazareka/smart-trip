package org.fruct.oss.smarttrip.points;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class TestPointsLoader implements PointsLoader {
	private static final double TEST_LAT = 61.78;
	private static final double TEST_LON = 34.35;
	private static final String TEST_NAME = "Test point";

	@Override
	public List<Point> loadPoints(double latCenter, double lonCenter, double radius, String pattern) {
		ArrayList<Point> ret = new ArrayList<>();

		float[] dist = new float[1];
		Location.distanceBetween(latCenter, lonCenter, TEST_LAT, TEST_LON, dist);

		if (dist[0] < radius) {
			ret.add(new Point(TEST_LAT, TEST_LON, TEST_NAME));
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {
		}

		return ret;
	}
}
