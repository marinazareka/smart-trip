package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.data.Movement;
import org.fruct.oss.tsp.events.ScheduleStoreChangedEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ScheduleListFragment extends BaseFragment {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private PointsAdapter adapter;

	@Nullable
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
		EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(ScheduleStoreChangedEvent event) {
		adapter.notifyDataSetChanged();
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new PointsAdapter());
	}


	class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.Holder> {
		@Override
		public PointsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(PointsAdapter.Holder holder, int position) {
			holder.bind(getScheduleStore().getCurrentSchedule().get(position));
		}

		@Override
		public int getItemCount() {
			return getScheduleStore().getCurrentSchedule().size();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Movement movement) {
				textView.setText("Movement from " + movement.getA().getTitle() + " to " + movement.getB().getTitle());
			}
		}
	}

}
