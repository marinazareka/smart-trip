package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.mvp.SearchMvpView;
import org.fruct.oss.tsp.mvp.SearchPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFragment extends BaseFragment implements SearchMvpView {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	@Bind(R.id.card_view)
	CardView cardView;

	private SearchPresenter presenter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupPresenter();
	}

	private void setupPresenter() {
		presenter = new SearchPresenter();
		presenter.setView(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		ButterKnife.bind(this, view);
		return view;
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		//recyclerView.setAdapter(adapter = new ScheduleAdapter());
	}


	@Override
	public void onResume() {
		super.onResume();
		presenter.start();
	}

	@Override
	public void onPause() {
		presenter.stop();
		super.onPause();
	}

	@OnClick(R.id.search_button)
	void onSearchClicked() {
		presenter.onSearch();
	}

	@Override
	public void setEmptyMode(boolean isEmptyModeEnabled) {
		cardView.setVisibility(isEmptyModeEnabled ? View.VISIBLE : View.GONE);
		recyclerView.setVisibility(isEmptyModeEnabled ? View.GONE : View.VISIBLE);
	}

	@Override
	public void setPointList(List<Point> pointList) {

	}
}
