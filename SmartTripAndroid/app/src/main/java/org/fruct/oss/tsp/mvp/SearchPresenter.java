package org.fruct.oss.tsp.mvp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.fruct.oss.tsp.BuildConfig;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.database.DatabaseRepo;
import org.fruct.oss.tsp.events.SearchStoreChangedEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.SearchStore;
import org.fruct.oss.tsp.util.Pref;
import org.fruct.oss.tsp.util.UserPref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

public class SearchPresenter implements Presenter<SearchMvpView> {
	private static final Logger log = LoggerFactory.getLogger(SearchPresenter.class);

	private final SharedPreferences pref;
	private final SearchStore searchStore;
	private final SmartSpace smartspace;
	private final DatabaseRepo databaseRepo;

	private SearchMvpView view;
	private Point lastSelectedPoint;

	public SearchPresenter(Context context, SearchStore searchStore,
						   SmartSpace smartspace, DatabaseRepo databaseRepo) {
		this.searchStore = searchStore;
		this.smartspace = smartspace;
		this.databaseRepo = databaseRepo;

		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void setView(SearchMvpView searchMvpView) {
		this.view = searchMvpView;
	}

	@Override
	public void start() {
		EventBus.getDefault().register(this);
		view.setPointList(searchStore.getPoints());
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(SearchStoreChangedEvent event) {
		view.setEmptyMode(false);
		view.setPointList(searchStore.getPoints());
		view.dismissSearchWaiter();
	}

	public void onSearchAction() {
		view.displaySearchDialog(null, UserPref.getRadius(pref));
	}

	public void onPointAddToCurrentSchedule(Point point) {
		lastSelectedPoint = point;
	}

	public void onPointAddToNewSchedule(Point point) {
		lastSelectedPoint = point;
		view.displayNewScheduleDialog();
	}

	public void search(int radius, String patternText) {
		smartspace.postSearchRequest(radius, patternText);
		view.displaySearchWaiter();
	}

	public void onNewScheduleDialogFinished(String title, TspType tspType) {
		if (BuildConfig.DEBUG && lastSelectedPoint == null)
			throw new AssertionError("New schedule dialog finished, but lastSelectedPoint is null");

		log.debug("New schedule {} {}", title, tspType);
		long insertedId = databaseRepo.insertSchedule(new Schedule(title, tspType));
		Pref.setCurrentSchedule(pref, insertedId);

		onPointAddToCurrentSchedule(lastSelectedPoint);
	}
}
