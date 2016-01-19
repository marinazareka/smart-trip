package org.fruct.oss.tsp.smartspace;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.events.RequestFailedEvent;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.fruct.oss.tsp.events.SearchEvent;
import org.fruct.oss.tsp.util.UserPref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

// TODO: сервис может быть не подключен во время запросов.

public class BoundSmartSpace implements SmartSpace, Handler.Callback, SharedPreferences.OnSharedPreferenceChangeListener {
	private static final Logger log = LoggerFactory.getLogger(BoundSmartSpace.class);

	private final Context context;
	private final SharedPreferences pref;

	private SmartSpaceServiceConnection connection;

	private Messenger messenger;

	private Handler handler;

	private long serviceStartDelay = -1;
	private Handler uiThreadHandler;

	public BoundSmartSpace(FragmentActivity activity) {
		this.context = activity.getApplicationContext();
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		handler = new Handler(this);
	}

	public void start() {
		pref.registerOnSharedPreferenceChangeListener(this);
		serviceStartDelay = -1;

		uiThreadHandler = new Handler(Looper.getMainLooper());

		Intent intent = new Intent(context, SmartSpaceService.class);
		context.bindService(intent, connection = new SmartSpaceServiceConnection(), 0);
		scheduleServiceStart();
	}

	private void scheduleServiceStart() {
		if (serviceStartDelay < 0) {
			serviceStartDelay = 1000;
			startSmartSpaceService();
		} else {
			uiThreadHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					startSmartSpaceService();
				}
			}, serviceStartDelay);

			serviceStartDelay *= 2;
		}
	}

	private void sendInitializeMessage() {
		Bundle args = new Bundle();
		args.putString("address", UserPref.getSibAddress(pref));
		args.putInt("port", UserPref.getSibPort(pref));
		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_INITIALIZE, args);
	}

	private void startSmartSpaceService() {
		log.debug("Starting smartspace service");
		context.startService(new Intent(context, SmartSpaceService.class));
	}

	public void stop() {
		uiThreadHandler.removeCallbacksAndMessages(null);
		context.unbindService(connection);
		context.stopService(new Intent(context, SmartSpaceService.class));
		pref.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void updateUserLocation(Location location) {
		if (messenger == null) {
			return;
		}

		Bundle data = new Bundle();
		data.putParcelable("location", location);

		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_POST_USER_LOCATION, data);
	}

	@Override
	public void postSearchRequest(double radius, String pattern) {
		Bundle data = new Bundle();
		data.putDouble("radius", radius);
		data.putString("pattern", pattern);

		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_POST_SEARCH_REQUEST, data);
	}

	@Override
	public void postScheduleRequest(List<Point> pointList, TspType tspType) {
		Bundle data = new Bundle();
		data.putParcelableArrayList("points", new ArrayList<>(pointList));
		data.putSerializable("type", tspType);
		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_POST_SCHEDULE_REQUEST, data);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case SmartSpaceService.CALLBACK_SEARCH_RESULT:
			msg.getData().setClassLoader(Point.class.getClassLoader());
			List<Point> points = msg.getData().getParcelableArrayList("points");
			EventBus.getDefault().post(new SearchEvent(points));
			break;

		case SmartSpaceService.CALLBACK_SCHEDULE_RESULT:
			msg.getData().setClassLoader(Point.class.getClassLoader());
			List<Movement> movements = msg.getData().getParcelableArrayList("movements");
			EventBus.getDefault().post(new ScheduleEvent(movements));
			break;

		case SmartSpaceService.CALLBACK_REQUEST_FAILED:
			String description = msg.getData().getString("description");
			EventBus.getDefault().post(new RequestFailedEvent(description));
			break;
		}

		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key) {
		case UserPref.PREF_SIB_ADDRESS:
		case UserPref.PREF_SIB_PORT:
			sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_SHUTDOWN, new Bundle());
			sendInitializeMessage();
			break;
		}
	}

	private class SmartSpaceServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			log.debug("Smart space service connected");
			messenger = new Messenger(service);
			sendCallback();
			sendInitializeMessage();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			log.warn("Smart space service connection lost");
			BoundSmartSpace.this.onServiceDisconnected();
		}
	}

	private void sendCallback() {
		Bundle data = new Bundle();
		data.putParcelable("messenger", new Messenger(handler));
		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_SET_CALLBACK_MESSENGER, data);
	}

	private void sendSmartSpaceMessage(int msgActionPostUserLocation, Bundle data) {
		Message message = Message.obtain(null, msgActionPostUserLocation);
		message.setData(data);
		if (messenger != null) {
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				onServiceDisconnected();
			}
		}
	}

	private void onServiceDisconnected() {
		if (messenger != null) {
			log.debug("Smart space service disconnected");
			messenger = null;
			scheduleServiceStart();
		}
	}
}
