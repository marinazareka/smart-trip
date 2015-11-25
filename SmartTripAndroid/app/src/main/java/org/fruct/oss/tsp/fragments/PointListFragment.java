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
import org.fruct.oss.tsp.data.Point;
import org.fruct.oss.tsp.events.LocationEvent;
import org.fruct.oss.tsp.smartspace.SmartSpace;
import org.fruct.oss.tsp.smartspace.TestSmartSpace;
import org.fruct.oss.tsp.viewmodel.DefaultGeoViewModel;
import org.fruct.oss.tsp.viewmodel.TestGeoViewModel;
import org.fruct.oss.tsp.viewmodel.GeoViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import de.greenrobot.event.EventBus;

public class PointListFragment extends Fragment implements GeoViewModel.Listener {
	private static final String TAG = "PointListFragment";

	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private GeoViewModel geoViewModel;
	private PointsAdapter adapter;

	private SmartSpace smartspace;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupSmartspace();
		createTripModel();
		setupOptionsMenu();
	}

	private void setupSmartspace() {
		smartspace = new TestSmartSpace(getActivity());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_point_list_fragment, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_search).setVisible(geoViewModel.isAnythingChecked());
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
		for (GeoViewModel.PointModel pointModel : geoViewModel.getPoints()) {
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
		geoViewModel = new DefaultGeoViewModel(getActivity(), smartspace);
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
		EventBus.getDefault().register(this);
		smartspace.postRequest(1, "qwer");
		geoViewModel.start();
		geoViewModel.registerListener(this);
	}

	@Override
	public void onPause() {
		geoViewModel.unregisterListener(this);
		geoViewModel.stop();
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(LocationEvent event) {
		smartspace.updateUserLocation(event.getLocation());
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new PointsAdapter());
	}

	@Override
	public void pointsUpdated(List<GeoViewModel.PointModel> points) {
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
			holder.bind(position, geoViewModel.getPoints().get(position));
		}

		@Override
		public int getItemCount() {
			return geoViewModel.getPoints().size();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			@Bind(R.id.check_box)
			CheckBox checkBox;

			GeoViewModel.PointModel pointModel;
			int position;

			public Holder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			public void bind(int position, GeoViewModel.PointModel pointModel) {
				this.pointModel = pointModel;
				this.position = position;
				textView.setText(pointModel.point.getTitle());
				checkBox.setChecked(pointModel.isChecked);
			}

			@OnCheckedChanged(R.id.check_box)
			void onCheckBoxChecked(boolean checked) {
				Log.d(TAG, "Checked");
				geoViewModel.setCheckedState(position, checked);
				getActivity().invalidateOptionsMenu();
			}
		}
	}
}
