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

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SchedulesFragment extends BaseFragment {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private ScheduleAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		loadAdDisplaySchedules();
	}

	private void loadAdDisplaySchedules() {
		adapter.setScheduleList(getDatabase().loadSchedules());
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new ScheduleAdapter());
	}

	class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.Holder> {
		private List<Schedule> scheduleList = Collections.emptyList();

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

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Schedule schedule) {
				textView.setText(schedule.getTitle());
			}
		}
	}

}
