package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.mvp.SearchMvpView;
import org.fruct.oss.tsp.mvp.SearchPresenter;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFragment extends BaseFragment implements SearchMvpView {
	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	@Bind(R.id.card_view)
	CardView cardView;

	private SearchPresenter presenter;

	private Adapter adapter;

	private MaterialDialog waiterDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupPresenter();
	}

	private void setupPresenter() {
		presenter = new SearchPresenter(getContext(), getSearchStore(), getSmartSpace());
		presenter.setView(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	private void setupRecyclerView() {
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
				LinearLayoutManager.VERTICAL, false));

		recyclerView.setAdapter(adapter = new Adapter());
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

	@OnClick(R.id.search_button)
	void onSearchClicked() {
		presenter.onSearchAction();
	}

	@Override
	public void setEmptyMode(boolean isEmptyModeEnabled) {
		cardView.setVisibility(isEmptyModeEnabled ? View.VISIBLE : View.GONE);
		recyclerView.setVisibility(isEmptyModeEnabled ? View.GONE : View.VISIBLE);
	}

	@Override
	public void setPointList(List<Point> pointList) {
		adapter.setPointList(pointList);
	}

	@Override
	public void displaySearchDialog(@Nullable String initialPattern, int initialRadius) {
		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
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
							presenter.search(radius, patternText);
							dialog.dismiss();
						}
					}
				})
				.autoDismiss(false)
				.show();

		if (TextUtils.isEmpty(initialPattern)) {
			((EditText) ButterKnife.findById(dialog, R.id.pattern_edit_text)).setText(initialPattern);
		}

		if (initialRadius > 0) {
			((EditText) ButterKnife.findById(dialog, R.id.radius_edit_text)).setText(String.valueOf(initialRadius));
		}
	}

	@Override
	public void displaySearchWaiter() {
		waiterDialog = new MaterialDialog.Builder(getActivity())
				.progress(true, 1)
				.show();
	}

	@Override
	public void dismissSearchWaiter() {
		if (waiterDialog != null) {
			waiterDialog.dismiss();
			waiterDialog = null;
		}
	}

	class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
		private List<Point> pointList = Collections.emptyList();

		@Override
		public Adapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new Holder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_list_simple, parent, false));
		}

		@Override
		public void onBindViewHolder(Adapter.Holder holder, int position) {
			holder.bind(pointList.get(position));
		}

		@Override
		public int getItemCount() {
			return pointList.size();
		}

		public void setPointList(List<Point> pointList) {
			this.pointList = pointList;
			notifyDataSetChanged();
		}

		class Holder extends RecyclerView.ViewHolder {
			@Bind(android.R.id.text1)
			TextView textView;

			private final View view;

			private Point point;

			public Holder(View itemView) {
				super(itemView);

				ButterKnife.bind(this, itemView);

				this.view = itemView;
			}

			public void bind(Point point) {
				textView.setText(point.getTitle());
				this.point = point;
			}

			@OnClick(R.id.root)
			void onItemClicked() {
				PopupMenu popupMenu = new PopupMenu(getContext(), view);
				popupMenu.inflate(R.menu.point);
				popupMenu.setOnMenuItemClickListener(new PointMenuListener(point));
				popupMenu.show();
			}
		}
	}

	private class PointMenuListener implements PopupMenu.OnMenuItemClickListener {
		private final Point point;

		public PointMenuListener(Point point) {
			this.point = point;
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_add_current_schedule:
				presenter.onPointAddToCurrentSchedule(point);
				break;

			case R.id.action_add_new_schedule:
				presenter.onPointAddToNewSchedule(point);
				break;

			default:
				return false;
			}

			return true;
		}
	}
}
