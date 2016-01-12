package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.mvp.SchedulesMvpView;
import org.fruct.oss.tsp.mvp.SchedulesPresenter;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SchedulesFragment extends BaseFragment implements SchedulesMvpView {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private SchedulesPresenter presenter;

	private ScheduleAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createPresenter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	private void createPresenter() {
		presenter = new SchedulesPresenter(getDatabase());
		presenter.setView(this);
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

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new ScheduleAdapter());
	}

	@Override
	public void setScheduleList(List<Schedule> scheduleList) {
		adapter.setScheduleList(scheduleList);
	}

	@Override
	public void setSelectedSchedule(Schedule schedule) {
		adapter.setSelectedSchedule(schedule);
	}

	class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.Holder> {
		private List<Schedule> scheduleList = Collections.emptyList();
		private Schedule selectedSchedule;

		@Override
		public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_simple, parent, false));
		}

		@Override
		public void onBindViewHolder(Holder holder, int position) {
			holder.bind(scheduleList.get(position));
		}

		@Override
		public int getItemCount() {
			return scheduleList.size();
		}

		private void setScheduleList(List<Schedule> scheduleList) {
			this.scheduleList = scheduleList;
			notifyDataSetChanged();
		}

		public void setSelectedSchedule(Schedule schedule) {
			selectedSchedule = schedule;
			notifyDataSetChanged();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			private Schedule schedule;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Schedule schedule) {
				this.schedule = schedule;
				textView.setText(schedule.getTitle());
				itemView.setSelected(schedule == selectedSchedule);
			}

			@OnClick(R.id.root)
			void onItemClicked() {
				presenter.onScheduleClicked(schedule);
			}
		}
	}

}
