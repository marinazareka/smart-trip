package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Point;

public class SearchPresenter implements Presenter<SearchMvpView> {
	private SearchMvpView view;

	@Override
	public void setView(SearchMvpView searchMvpView) {
		this.view = searchMvpView;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	public void onSearch() {
		view.setEmptyMode(false);
	}

	public void onPointAddToSchedule(Point point) {
	}

	public void onPointAddToNewSchedule(Point point) {

	}
}
