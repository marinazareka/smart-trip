package org.fruct.oss.smarttrip;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.fruct.oss.smarttrip.jni.PointList;
import org.fruct.oss.smarttrip.jni.Smart;
import org.fruct.oss.smarttrip.points.Point;
import org.fruct.oss.smarttrip.points.PointsLoader;
import org.fruct.oss.smarttrip.points.SmartPointsLoader;
import org.fruct.oss.smarttrip.points.TestPointsLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartService extends Service {
	private static final String TAG = "SmartService";

	public static final String ACTION_QUERY_POINTS = "org.fruct.oss.smarttrip.ACTION_QUERY_POINTS";

	public static final int RESPONSE_POINTS = 1001;
	public static final String RESPONSE_EXTRA_POINTS = "points";

	public static final String EXTRA_REPLY_TO = "org.fruct.oss.smarttrip.EXTRA_REPLY_TO";
	public static final String EXTRA_LAT = "org.fruct.oss.smarttrip.EXTRA_LAT";
	public static final String EXTRA_LON = "org.fruct.oss.smarttrip.EXTRA_LON";
	public static final String EXTRA_RADIUS = "org.fruct.oss.smarttrip.EXTRA_RADIUS";

	private boolean isSmartSpaceConnected;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	static {
		System.loadLibrary("Smart");
	}

	public SmartService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_NOT_STICKY;
		}

		String action = intent.getAction();
		if (action == null) {
			return START_NOT_STICKY;
		}

		switch (action) {
		case ACTION_QUERY_POINTS:
			final double lat = intent.getDoubleExtra(EXTRA_LAT, -180);
			final double lon = intent.getDoubleExtra(EXTRA_LON, -180);
			final double radius = intent.getDoubleExtra(EXTRA_RADIUS, -1);
			final Messenger messenger = intent.getParcelableExtra(EXTRA_REPLY_TO);

			if (lat < -180 || lon < -180 || radius < 0 || messenger == null) {
				Log.w(TAG, "ACTION_QUERY_POINTS without argument");
				return START_NOT_STICKY;
			}

			executor.execute(new Runnable() {
				@Override
				public void run() {
					doActionQueryPoints(lat, lon, radius, messenger);
				}
			});

			break;
		}

		return START_NOT_STICKY;
	}

	private void doActionQueryPoints(double lat, double lon, double radius, Messenger messenger) {
		PointList pointList = Smart.loadPoints(lat, lon, radius);

		List<Point> retPoints = new ArrayList<>();

		for (int i = 0; i < pointList.size(); i++) {
			org.fruct.oss.smarttrip.jni.Point swigPoint = pointList.get(i);
			retPoints.add(new Point(swigPoint.getLat(), swigPoint.getLon(), swigPoint.getName()));
		}

		// TODO: need delete points also?
		pointList.delete();

		Log.d(TAG, "Points received. Count = " + retPoints.size());
		sendPointsResponse(retPoints, messenger);
	}

	private void sendPointsResponse(List<Point> points, Messenger messenger) {
		Message message = Message.obtain(null, RESPONSE_POINTS);

		Bundle responseExtras = new Bundle();
		responseExtras.putParcelableArrayList(RESPONSE_EXTRA_POINTS, new ArrayList<>(points));

		message.setData(responseExtras);
		try {
			messenger.send(message);
		} catch (RemoteException e) {
			Log.w(TAG, "Can't send response to caller");
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		executor.execute(new Runnable() {
			@Override
			public void run() {
				Smart.connect("X", "172.20.2.240", 10622);
				Log.w(TAG, "Can't connect to smartspace");
			}
		});
	}

	@Override
	public void onDestroy() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (isSmartSpaceConnected) {
					Smart.disconnect();
				}
			}
		});

		executor.shutdown();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Can't bind to this service");
	}
}
