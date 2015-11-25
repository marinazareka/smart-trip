package org.fruct.oss.tsp.viewmodel;

import android.content.Context;

import org.fruct.oss.tsp.events.SearchEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;

import de.greenrobot.event.EventBus;

public class DefaultGeoViewModel extends AbstractGeoViewModel {
	private Context context;
	private SmartSpace smartSpace;

	public DefaultGeoViewModel(Context context, SmartSpace smartSpace) {
		this.context = context;
		this.smartSpace = smartSpace;
	}

	@Override
	public void start() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(SearchEvent event) {
		updatePoints(event.getPoints());
	}
}
