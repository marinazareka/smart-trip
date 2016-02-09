package org.fruct.oss.tsp.smartspace;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.SmartSpaceNative;
import org.fruct.oss.tsp.util.Pref;
import org.fruct.oss.tsp.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartSpaceService extends Service implements Handler.Callback {
	private static final Logger log = LoggerFactory.getLogger(SmartSpaceService.class);

	public static final int MSG_ACTION_INITIALIZE = 0;
	public static final int MSG_ACTION_SHUTDOWN = 1;
	public static final int MSG_ACTION_POST_USER_LOCATION = 2;
	public static final int MSG_ACTION_POST_SEARCH_REQUEST = 3;
	public static final int MSG_ACTION_POST_SCHEDULE_REQUEST = 4;
	public static final int MSG_ACTION_SET_CALLBACK_MESSENGER = 5;

	public static final int CALLBACK_SCHEDULE_RESULT = 5;
	public static final int CALLBACK_HISTORY_RESULT = 6;
	public static final int CALLBACK_SEARCH_RESULT = 7;
	public static final int CALLBACK_REQUEST_FAILED = 8;

	private SharedPreferences pref;

	private Messenger messenger;

	private Handler handler;
	private HandlerThread handlerThread;

	private SmartSpaceNative smartSpace;

	private Messenger callbackMessenger;

	private boolean isInitialized;
	private boolean isPanicScheduled;

	public SmartSpaceService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		log.debug("onCreate");

		pref = PreferenceManager.getDefaultSharedPreferences(this);

		handlerThread = new HandlerThread("smart-space-handler");
		handlerThread.start();

		handler = new Handler(handlerThread.getLooper(), this);
		messenger = new Messenger(handler);

		smartSpace = createSmartSpaceNative();
	}

	@Override
	public void onDestroy() {
		handlerThread.getLooper().quit();
		smartSpace.shutdown();
		isInitialized = false;

		log.debug("onDestroy");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	private SmartSpaceNative createSmartSpaceNative() {
		return SmartSpaceNativeLoader.createSmartSpaceNative();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_ACTION_SHUTDOWN:
			if (isInitialized) {
				smartSpace.shutdown();
				isInitialized = false;
			}
			break;

		case MSG_ACTION_INITIALIZE:
			try {
				String address = msg.getData().getString("address");
				int port = msg.getData().getInt("port");

				smartSpace.setListener(new Listener());
				smartSpace.initialize(Pref.getUserId(pref), "android-user-kp-" + Pref.getUserId(pref),
						"X", address, port);
				isInitialized = true;
			} catch (IOException e) {
				log.error("Can't initialize smartspace", e);
			}
			break;

		case MSG_ACTION_POST_USER_LOCATION:
			Location location = msg.getData().getParcelable("location");
			handlePostUserLocation(location);
			break;

		case MSG_ACTION_POST_SEARCH_REQUEST:
			double radius = msg.getData().getDouble("radius");
			String pattern = msg.getData().getString("pattern");
			handlePostSearchRequest(radius, pattern);
			break;

		case MSG_ACTION_POST_SCHEDULE_REQUEST:
			msg.getData().setClassLoader(Point.class.getClassLoader());
			List<Point> points = msg.getData().getParcelableArrayList("points");
			Schedule schedule = (Schedule) msg.getData().getSerializable("schedule");
			handlerPostScheduleRequest(points, schedule);
			break;

		case MSG_ACTION_SET_CALLBACK_MESSENGER:
			Messenger callbackMessenger = msg.getData().getParcelable("messenger");
			handleSetCallbackMessenger(callbackMessenger);
			break;
		}

		if (isPanicScheduled) {
			log.error("Something wrong with smartspace, stopping");
			System.exit(0);
		}

		return true;
	}

	private void panic() {
		isPanicScheduled = true;
	}

	private void handlePostUserLocation(Location location) {
		if (!isInitialized)
			return;

		try {
			smartSpace.updateUserLocation(location.getLatitude(), location.getLongitude());
		} catch (IOException e) {
			log.error("Error posting location", e);
			panic();
		}
	}

	private void handlePostSearchRequest(double radius, String pattern) {
		if (!isInitialized)
			return;

		try {
			smartSpace.postSearchRequest(radius, pattern);
		} catch (IOException e) {
			log.error("Error posting search request", e);
			panic();
		}
	}

	private void handlerPostScheduleRequest(List<Point> points, Schedule schedule) {
		if (!isInitialized)
			return;

		try {
			smartSpace.postScheduleRequest(points.toArray(new Point[points.size()]),
					schedule.getTspType().name().toLowerCase(), schedule.getRoadType(),
					Utils.localDateTimeToString(schedule.getStartDateTime()),
					Utils.localDateTimeToString(schedule.getEndDateTime()));
		} catch (IOException e) {
			log.error("Error posting schedule request", e);
			panic();
		}
	}

	private void handleSetCallbackMessenger(Messenger callbackMessenger) {
		this.callbackMessenger = callbackMessenger;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}


	private class Listener implements SmartSpaceNative.Listener {
		@Override
		public void onSearchRequestReady(final Point[] points) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (callbackMessenger != null) {
						ArrayList<Point> points1 = new ArrayList<>(Arrays.asList(points));

						Bundle data = new Bundle();
						data.putParcelableArrayList("points", points1);

						Message message = Message.obtain(null, CALLBACK_SEARCH_RESULT);
						message.setData(data);

						try {
							callbackMessenger.send(message);
						} catch (RemoteException e) {
							callbackMessenger = null;
							log.error("Callback messenger disconnected");
						}
					}
				}
			});
		}

		@Override
		public void onScheduleRequestReady(final Movement[] movements) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (callbackMessenger != null) {
						ArrayList<Movement> movements1 = new ArrayList<>(Arrays.asList(movements));

						Bundle data = new Bundle();
						data.putParcelableArrayList("movements", movements1);

						Message message = Message.obtain(null, CALLBACK_SCHEDULE_RESULT);
						message.setData(data);

						try {
							callbackMessenger.send(message);
						} catch (RemoteException e) {
							callbackMessenger = null;
							log.error("Callback messenger disconnected");
						}
					}
				}
			});
		}

		@Override
		public void onSearchHistoryReady(final String[] patterns) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					ArrayList<String> patterns1 = new ArrayList<>(Arrays.asList(patterns));

					Bundle data = new Bundle();
					data.putStringArrayList("patterns", patterns1);

					Message message = Message.obtain(null, CALLBACK_HISTORY_RESULT);
					message.setData(data);

					try {
						callbackMessenger.send(message);
					} catch (RemoteException e) {
						callbackMessenger = null;
						log.error("Callback messenger disconnected");
					}
				}
			});
		}

		@Override
		public void onRequestFailed(final String description) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (callbackMessenger != null) {
						try {
							Bundle data = new Bundle();
							data.putString("description", description);
							Message message = Message.obtain(null, CALLBACK_REQUEST_FAILED);
							message.setData(data);
							callbackMessenger.send(message);
						} catch (RemoteException e) {
							callbackMessenger = null;
							log.error("Callback messenger disconnected");
						}
					}
				}
			});
		}
	}
}
