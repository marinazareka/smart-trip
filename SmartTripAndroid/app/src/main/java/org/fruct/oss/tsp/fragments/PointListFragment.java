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

import butterknife.Bind;
import butterknife.ButterKnife;

public class PointListFragment extends Fragment {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(new PointsAdapter());
	}


	static class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.Holder> {
		@Override
		public PointsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(PointsAdapter.Holder holder, int position) {
			holder.bind(new Point(Math.random(), Math.random()));
		}

		@Override
		public int getItemCount() {
			return 100500;
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(Point point) {
				textView.setText("Point " + point.getLat() + " " + point.getLon());
			}
		}

	}

}
