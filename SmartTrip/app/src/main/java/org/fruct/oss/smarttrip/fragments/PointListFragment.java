package org.fruct.oss.smarttrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fruct.oss.smarttrip.EventReceiver;
import org.fruct.oss.smarttrip.R;
import org.fruct.oss.smarttrip.events.PointClickedEvent;
import org.fruct.oss.smarttrip.events.PointsLoadedEvent;
import org.fruct.oss.smarttrip.points.Point;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

class PointAdapter extends RecyclerView.Adapter<PointAdapter.Holder> {
	private final LayoutInflater inflater;

	private List<Point> points = new ArrayList<>();

	public PointAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	@Override
	public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		final Holder holder = new Holder(itemView);

		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new PointClickedEvent(holder.point));
			}
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(Holder holder, int position) {
		holder.point = points.get(position);
		holder.textView.setText(holder.point.getName());
	}

	@Override
	public int getItemCount() {
		return points.size();
	}

	public void setPoints(List<Point> points) {
		this.points = points;
		notifyDataSetChanged();
	}

	class Holder extends RecyclerView.ViewHolder {
		private final TextView textView;

		private Point point;

		public Holder(View itemView) {
			super(itemView);

			textView = (TextView) itemView.findViewById(android.R.id.text1);
		}
	}
}

public class PointListFragment extends Fragment {
	private RecyclerView recyclerView;
	private PointAdapter adapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_point_list,
				container, false);
		recyclerView.setHasFixedSize(true);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(linearLayoutManager);

		adapter = new PointAdapter(getActivity());
		recyclerView.setAdapter(adapter);
		return recyclerView;
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().registerSticky(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@EventReceiver
	public void onEventMainThread(PointsLoadedEvent event) {
		adapter.setPoints(event.getPoints());
	}
}
