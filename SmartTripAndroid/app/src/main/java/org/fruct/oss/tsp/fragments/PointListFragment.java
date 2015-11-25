package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.model.Point;
import org.fruct.oss.tsp.model.TestGeoModel;
import org.fruct.oss.tsp.model.GeoModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class PointListFragment extends Fragment implements GeoModel.Listener {
	private static final String TAG = "PointListFragment";

	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private GeoModel geoModel;
	private PointsAdapter adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createTripModel();
		setupOptionsMenu();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_point_list_fragment, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_search).setVisible(geoModel.isAnythingChecked());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			searchSelection();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void searchSelection() {
		List<Point> checkedPoints = new ArrayList<>();
		for (GeoModel.PointModel pointModel : geoModel.getPoints()) {
			if (pointModel.isChecked) {
				checkedPoints.add(pointModel.point);
			}
		}
		Log.d(TAG, checkedPoints.size() + " points searching");
	}

	private void setupOptionsMenu() {
		setHasOptionsMenu(true);
	}

	private void createTripModel() {
		geoModel = new TestGeoModel();
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
		geoModel.start();
		geoModel.registerListener(this);
	}

	@Override
	public void onPause() {
		geoModel.unregisterListener(this);
		geoModel.stop();
		super.onPause();
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new PointsAdapter());
	}

	@Override
	public void pointsUpdated(List<GeoModel.PointModel> points) {
		adapter.notifyDataSetChanged();
	}

	class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.Holder> {
		@Override
		public PointsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_point, parent, false));
		}

		@Override
		public void onBindViewHolder(PointsAdapter.Holder holder, int position) {
			holder.bind(position, geoModel.getPoints().get(position));
		}

		@Override
		public int getItemCount() {
			return geoModel.getPoints().size();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			@Bind(R.id.check_box)
			CheckBox checkBox;

			GeoModel.PointModel pointModel;
			int position;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(int position, GeoModel.PointModel pointModel) {
				this.pointModel = pointModel;
				this.position = position;
				textView.setText(pointModel.point.getTitle());
				checkBox.setChecked(pointModel.isChecked);
			}

			@OnCheckedChanged(R.id.check_box)
			void onCheckBoxChecked(boolean checked) {
				Log.d(TAG, "Checked");
				geoModel.setCheckedState(position, checked);
				getActivity().invalidateOptionsMenu();
			}
		}
	}
}
