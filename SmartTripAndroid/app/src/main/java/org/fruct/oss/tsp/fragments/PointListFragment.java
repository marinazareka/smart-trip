package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.data.Point;
import org.fruct.oss.tsp.viewmodel.DefaultGeoViewModel;
import org.fruct.oss.tsp.viewmodel.GeoViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Пользовательский интерфейс для списка точек для последнего запроса пользователя
 *
 * Содержит следующие элементы
 * <ul>
 *     <li>Пункт меню "Поиск". При выборе приложение запрашивает у пользователя
 *     параметры поиска и передает запрос {@link org.fruct.oss.tsp.smartspace.SmartSpace}</li>
 *     <li>Список точек с checkbox'ми выбора точек для построения маршрута</li>
 *     <li>Пункт меню "Маршрут". Активен при наличии выбранных точек.</li>
 * </ul>
 *
 * Текущее состояние выбора точек и хранится в объекте вспомогательного класса {@link GeoViewModel}.
 */
public class PointListFragment extends BaseFragment implements GeoViewModel.Listener {
	private static final String TAG = "PointListFragment";

	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	private GeoViewModel geoViewModel;
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
		menu.findItem(R.id.action_schedule).setVisible(geoViewModel.isAnythingChecked());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_schedule:
			scheduleSelection();
			break;

		case R.id.action_search:
			startSearchDialog();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void startSearchDialog() {
		new MaterialDialog.Builder(getActivity())
				.title(R.string.title_enter_request)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.customView(R.layout.dialog_search_request, false)
				.onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						dialog.dismiss();
					}
				})
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog,
										@NonNull DialogAction which) {
						EditText radiusEditText = ButterKnife.findById(dialog.getView(),
								R.id.radius_edit_text);
						EditText patternEditText = ButterKnife.findById(dialog.getView(),
								R.id.pattern_edit_text);

						String radiusText = radiusEditText.getText().toString();
						String patternText = patternEditText.getText().toString();

						int radius = -1;
						try {
							radius = Integer.parseInt(radiusText);
						} catch (NumberFormatException ex) {
							radiusEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
						}

						if (TextUtils.isEmpty(patternText)) {
							patternEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
						}

						if (!TextUtils.isEmpty(patternText) && radius >= 0) {
							search(radius, patternText);
							dialog.dismiss();
						}
					}
				})
				.autoDismiss(false)
				.show();
	}

	private void search(int radius, String patternText) {
		getSmartSpace().postSearchRequest(radius, patternText);
	}

	private void scheduleSelection() {
		List<Point> checkedPoints = new ArrayList<>();
		for (GeoViewModel.PointModel pointModel : geoViewModel.getPoints()) {
			if (pointModel.isChecked) {
				checkedPoints.add(pointModel.point);
			}
		}
		Log.d(TAG, checkedPoints.size() + " points searching");

		getSmartSpace().postScheduleRequest(checkedPoints);
	}

	private void setupOptionsMenu() {
		setHasOptionsMenu(true);
	}

	private void createTripModel() {
		geoViewModel = new DefaultGeoViewModel(getActivity(), getGeoStore());
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


		geoViewModel.start();
		geoViewModel.registerListener(this);
	}

	@Override
	public void onPause() {
		geoViewModel.unregisterListener(this);
		geoViewModel.stop();
		super.onPause();
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
