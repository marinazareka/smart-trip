package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.events.GeoStoreChangedEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.GeoStore;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class PointListPresenter {
	private PointListMvpView view;
	private GeoStore geoStore;
	private SmartSpace smartspace;

	private List<Point> checkedPoints = new ArrayList<>();

	public PointListPresenter(GeoStore geoStore, SmartSpace smartspace) {
		this.geoStore = geoStore;
		this.smartspace = smartspace;
	}

	public void setView(PointListMvpView pointListMvpView) {
		this.view = pointListMvpView;
	}

	public void start() {
		view.setPointList(geoStore.getPoints());
		EventBus.getDefault().register(this);
	}

	public void stop() {
		EventBus.getDefault().unregister(this);

	}

	public void onSearchMenuAction() {
		view.displaySearchDialog();
	}

	public void onEventMainThread(GeoStoreChangedEvent event) {
		//view.setPointList(geoStore.getPoints());
		view.displayFoundPoints(geoStore.getPoints());
		view.dismissSearchWaiter();
	}

	public void search(int radius, String patternText) {
		smartspace.postSearchRequest(radius, patternText);
		view.displaySearchWaiter();
	}

	public void onPointsChecked(List<Point> checkedPoints) {
		this.checkedPoints.addAll(checkedPoints);
		view.setPointList(new ArrayList<>(this.checkedPoints));

	}
}
