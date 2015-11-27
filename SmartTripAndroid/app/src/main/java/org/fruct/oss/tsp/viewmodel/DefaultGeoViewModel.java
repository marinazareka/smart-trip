package org.fruct.oss.tsp.viewmodel;

import android.content.Context;

import org.fruct.oss.tsp.events.GeoStoreChangedEvent;
import org.fruct.oss.tsp.events.SearchEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;

import de.greenrobot.event.EventBus;

/**
 * Реализация {@link GeoViewModel} с привязкой к хранилищу данных {@link GeoStore}
 */
public class DefaultGeoViewModel extends AbstractGeoViewModel {
	private Context context;
	private GeoStore geoStore;

	public DefaultGeoViewModel(Context context, GeoStore geoStore) {
		this.context = context;
		this.geoStore = geoStore;
	}

	@Override
	public void start() {
		EventBus.getDefault().register(this);
		refresh();
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(GeoStoreChangedEvent event) {
		refresh();
	}

	private void refresh() {
		updatePoints(geoStore.getPoints());

	}
}
