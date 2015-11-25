package org.fruct.oss.tsp.smartspace;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import org.fruct.oss.tsp.data.Point;
import org.fruct.oss.tsp.data.SearchRequest;
import org.fruct.oss.tsp.data.User;
import org.fruct.oss.tsp.events.SearchEvent;

import java.util.ArrayList;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class TestSmartSpace implements SmartSpace {
	private Random random;

	private Location location;

	private double radius;
	private String pattern;

	private Context context;
	private Handler handler;

	public TestSmartSpace(Context context) {
		this.context = context.getApplicationContext();
		handler = new Handler();
		random = new Random();
	}

	@Override
	public void publishUser(User user) {
	}

	@Override
	public void updateUserLocation(Location location) {
		this.location = location;
		search();
	}

	@Override
	public SearchRequest postRequest(double radius, String pattern) {
		this.radius = radius;
		this.pattern = pattern;

		search();

		return new SearchRequest();
	}

	void search() {
		if (location == null || pattern == null) {
			return;
		}

		ArrayList<Point> points = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			double lat = location.getLatitude() + (2 * random.nextDouble() - 1) * 0.01;
			double lon = location.getLongitude() + (2 * random.nextDouble() - 1) * 0.01;
			String id = "id" + lat + ":" + lon;
			String title = "title" + lat + ":" + lon;
			points.add(new Point(id, title, lat, lon));
		}

		EventBus.getDefault().post(new SearchEvent(points));
	}
}
