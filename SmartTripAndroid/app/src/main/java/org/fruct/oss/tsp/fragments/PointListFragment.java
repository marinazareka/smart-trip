package org.fruct.oss.tsp.fragments;

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
import org.fruct.oss.tsp.model.Point;
import org.fruct.oss.tsp.model.TestTripModel;
import org.fruct.oss.tsp.model.TripModel;
import org.fruct.oss.tsp.smartslognative.NativeTest;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PointListFragment extends Fragment implements TripModel.Listener {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private TripModel tripModel;
	private PointsAdapter adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createTripModel();
	}

	private void createTripModel() {
		tripModel = new TestTripModel();
	}

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
		tripModel.start();
		tripModel.registerListener(this);
	}

	@Override
	public void onPause() {
		tripModel.unregisterListener(this);
		tripModel.stop();
		super.onPause();
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new PointsAdapter());
	}

	@Override
	public void pointsUpdated(List<Point> points) {
		adapter.notifyDataSetChanged();
	}

	class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.Holder> {
		@Override
		public PointsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(PointsAdapter.Holder holder, int position) {
			holder.bind(tripModel.getPoints().get(position));
		}

		@Override
		public int getItemCount() {
			return tripModel.getPoints().size();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Point point) {
				textView.setText(point.getLat() + " " + point.getLon());
			}
		}

	}

}
