package org.fruct.oss.tsp.fragments.secondary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.adapters.PointAdapter;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.fragments.AddPointFragment;
import org.fruct.oss.tsp.fragments.BaseFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ScheduleDetailsFragment extends BaseFragment implements PointAdapter.Listener {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	@Bind(R.id.dialog_anchor_container)
	View dialogAnchorContainer;

	private PointAdapter adapter;

	private Schedule schedule;
	private Subscription pointsSubscription;

	public static ScheduleDetailsFragment newInstance(Schedule schedule) {
		Bundle args = new Bundle(1);
		args.putSerializable("schedule", schedule);

		ScheduleDetailsFragment fragment = new ScheduleDetailsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			schedule = (Schedule) getArguments().getSerializable("schedule");
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		pointsSubscription = getDatabase().loadSchedulePoints(schedule.getId())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Point>>() {
					@Override
					public void call(List<Point> points) {
						adapter.setPointList(points);
					}
				});
	}

	@Override
	public void onPause() {
		pointsSubscription.unsubscribe();
		super.onPause();
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new PointAdapter(this));
	}

	@Override
	public void onPointClicked(Point point, View anchorView) {
		dialogAnchorContainer.setX(anchorView.getX());
		dialogAnchorContainer.setY(anchorView.getY());

		AddPointFragment.addToFragmentManager(
				AddPointFragment.newInstance(point),
				getFragmentManager(),
				R.id.dialog_anchor_container
		);
	}
}
