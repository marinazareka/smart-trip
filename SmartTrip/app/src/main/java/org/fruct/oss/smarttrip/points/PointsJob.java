package org.fruct.oss.smarttrip.points;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.fruct.oss.smarttrip.events.PointsFailedEvent;
import org.fruct.oss.smarttrip.events.PointsLoadedEvent;
import org.fruct.oss.smarttrip.events.PointsStartedEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

public class PointsJob extends Job {
	private PointsLoader pointsLoader;

	private static final AtomicInteger jobIncrement = new AtomicInteger();

	private final int jobId;

	private final double latitude;
	private final double longitude;
	private final double radius;

	public PointsJob(PointsLoader pointsLoader, double latitude, double longitude, double radius) {
		super(new Params(1000).requireNetwork().groupBy("points-job"));

		this.pointsLoader = pointsLoader;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;

		this.jobId = jobIncrement.incrementAndGet();
	}

	@Override
	public void onAdded() {
	}

	@Override
	public void onRun() throws Throwable {
		if (checkJobObsolete()) {
			return;
		}
		EventBus.getDefault().post(new PointsStartedEvent());
		List<Point> points = pointsLoader.loadPoints(latitude, longitude, radius);
		EventBus.getDefault().post(new PointsLoadedEvent(points));
	}

	private boolean checkJobObsolete() {
		return jobIncrement.get() != jobId;
	}

	@Override
	protected void onCancel() {
		EventBus.getDefault().post(new PointsFailedEvent());
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return false;
	}

	@Override
	protected int getRetryLimit() {
		return 0;
	}
}
