package org.fruct.oss.tsp.smartspace;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.data.ScheduleRequest;
import org.fruct.oss.tsp.data.SearchRequest;
import org.fruct.oss.tsp.data.User;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.fruct.oss.tsp.events.SearchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

// TODO: сервис может быть не подключен во время запросов.

public class BoundSmartSpace implements SmartSpace, Handler.Callback {
	private static final Logger log = LoggerFactory.getLogger(BoundSmartSpace.class);

	private final Context context;

	private SmartSpaceServiceConnection connection;

	private Messenger messenger;
	private Handler handler;

	public BoundSmartSpace(FragmentActivity activity) {
		this.context = activity.getApplicationContext();
		handler = new Handler(this);
	}

	public void start() {
		Intent intent = new Intent(context, SmartSpaceService.class);
		context.bindService(intent, connection = new SmartSpaceServiceConnection(), Context.BIND_AUTO_CREATE);
	}

	public void stop() {
		context.unbindService(connection);
	}

	@Override
	public void publishUser(User user) {

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
	public SearchRequest postSearchRequest(double radius, String pattern) {
		Bundle data = new Bundle();
		data.putDouble("radius", radius);
		data.putString("pattern", pattern);

		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_POST_SEARCH_REQUEST, data);
		return new SearchRequest();
	}

	@Override
	public ScheduleRequest postScheduleRequest(List<Point> pointList) {
		Bundle data = new Bundle();
		data.putParcelableArrayList("points", new ArrayList<>(pointList));

		sendSmartSpaceMessage(SmartSpaceService.MSG_ACTION_POST_SCHEDULE_REQUEST, data);
		return new ScheduleRequest();
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
		}

		return true;
	}

	private class SmartSpaceServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			log.debug("Smart space service connected");
			messenger = new Messenger(service);
			sendCallback();
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
		log.debug("Smart space service disconnected");
		messenger = null;
	}
}
