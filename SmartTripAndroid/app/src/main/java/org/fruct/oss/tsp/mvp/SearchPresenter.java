package org.fruct.oss.tsp.mvp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.events.SearchStoreChangedEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.stores.SearchStore;
import org.fruct.oss.tsp.util.Pref;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class SearchPresenter implements Presenter<SearchMvpView> {
	private final SharedPreferences pref;
	private final SearchStore searchStore;
	private final SmartSpace smartspace;

	private SearchMvpView view;

	public SearchPresenter(Context context, SearchStore searchStore, SmartSpace smartspace) {
		this.searchStore = searchStore;
		this.smartspace = smartspace;

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
		view.displaySearchDialog(null, Pref.getRadius(pref));
	}

	public void onPointAddToCurrentSchedule(Point point) {

	}

	public void onPointAddToNewSchedule(Point point) {

	}

	public void search(int radius, String patternText) {
		smartspace.postSearchRequest(radius, patternText);
		view.displaySearchWaiter();
	}
}
