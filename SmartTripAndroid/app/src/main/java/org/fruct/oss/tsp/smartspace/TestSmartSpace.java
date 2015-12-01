package org.fruct.oss.tsp.smartspace;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import org.fruct.oss.tsp.commondatatypes.Movement;
import org.fruct.oss.tsp.commondatatypes.Point;
import org.fruct.oss.tsp.data.ScheduleRequest;
import org.fruct.oss.tsp.data.SearchRequest;
import org.fruct.oss.tsp.data.User;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.fruct.oss.tsp.events.SearchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * Реализация интерфейса {link SmartSpace} для тестирования и отладки
 */
public class TestSmartSpace implements SmartSpace {
	private static final String TAG = "TestSmartSpace";

	private Random random;

	private Location location;

	private double radius;
	private String pattern;
	private boolean isSearchFinished;

	private List<Point> schedulePoints;

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
	public SearchRequest postSearchRequest(double radius, String pattern) {
		this.radius = radius;
		this.pattern = pattern;

		search();

		return new SearchRequest();
	}

	@Override
	public ScheduleRequest postScheduleRequest(List<Point> pointList) {
		this.schedulePoints = pointList;

		schedule();

		return new ScheduleRequest();
	}

	void search() {
		Log.d(TAG, "Searching with " + location + ":" + pattern + ":" + radius);

		if (location == null || pattern == null) {
			return;
		}

		if (isSearchFinished) {
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

		isSearchFinished = true;
		EventBus.getDefault().post(new SearchEvent(points));
	}

	void schedule() {
		if (schedulePoints == null) {
			return;
		}

		List<Movement> movements = new ArrayList<>();
		for (int i = 1; i < schedulePoints.size(); i++) {
			movements.add(new Movement(schedulePoints.get(i - 1), schedulePoints.get(i)));
		}

		EventBus.getDefault().post(new ScheduleEvent(movements));
	}
}
