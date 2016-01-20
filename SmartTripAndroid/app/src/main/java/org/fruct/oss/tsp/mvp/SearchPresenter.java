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

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SearchPresenter implements Presenter<SearchMvpView> {
	private static final Logger log = LoggerFactory.getLogger(SearchPresenter.class);

	private final SharedPreferences pref;
	private final SearchStore searchStore;
	private final SmartSpace smartspace;
	private final DatabaseRepo databaseRepo;

	private SearchMvpView view;
	private Point lastSelectedPoint;
	private Subscription foundPointsSubscription;

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
		foundPointsSubscription = searchStore.getObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Point>>() {
					@Override
					public void call(List<Point> points) {
						view.setEmptyMode(points.isEmpty());
						view.setPointList(points);
						view.dismissSearchWaiter();
					}
				});
	}

	@Override
	public void stop() {
		foundPointsSubscription.unsubscribe();
	}

	public void onSearchAction() {
		view.displaySearchDialog(null, UserPref.getRadius(pref));
	}

	public void search(int radius, String patternText) {
		smartspace.postSearchRequest(radius, patternText);
		view.displaySearchWaiter();
	}

}
