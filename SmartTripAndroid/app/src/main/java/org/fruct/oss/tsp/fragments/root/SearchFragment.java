package org.fruct.oss.tsp.fragments.root;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.activities.MainActivity;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.events.HistoryAppendEvent;
import org.fruct.oss.tsp.fragments.AddPointFragment;
import org.fruct.oss.tsp.fragments.BaseFragment;
import org.fruct.oss.tsp.fragments.secondary.ScheduleDetailsFragment;
import org.fruct.oss.tsp.util.UserPref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SearchFragment extends BaseFragment {
	private static final Logger log = LoggerFactory.getLogger(SearchFragment.class);

	@Bind(R.id.recycler_view)
	RecyclerView recyclerView;

	@Bind(R.id.dialog_anchor_container)
	View dialogAnchorContainer;

	private SharedPreferences pref;

	private MenuItem searchMenuItem;

	private Adapter adapter;

	private MaterialDialog waiterDialog;

	private Subscription testSubscription;
	private Subscription foundPointsSubscription;
	private Subscription updatedSubscription;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		pref = PreferenceManager.getDefaultSharedPreferences(getContext());
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		ButterKnife.bind(this, view);
		setupRecyclerView();
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search, menu);

		searchMenuItem = menu.findItem(R.id.search);

		SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.test:
			((MainActivity) getActivity()).switchSecondaryFragment(new ScheduleDetailsFragment());
			return true;
		}

		return false;
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

		testSubscription = getHistoryStore().getObservable()
				.subscribe(new Action1<List<String>>() {
					@Override
					public void call(List<String> strings) {
						for (String string : strings) {
							log.debug("History loaded {}", string);
						}
					}
				});
		foundPointsSubscription = getSearchStore().getObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Point>>() {
					@Override
					public void call(List<Point> points) {
						setPointList(points);
					}
				});
		updatedSubscription = getSearchStore().getUpdatedObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Object>() {
					@Override
					public void call(Object o) {
						dismissSearchWaiter();
					}
				});

	}

	@Override
	public void onPause() {
		testSubscription.unsubscribe();
		foundPointsSubscription.unsubscribe();
		updatedSubscription.unsubscribe();

		super.onPause();
	}

	private void setPointList(List<Point> pointList) {
		adapter.setPointList(pointList);
	}

	private void displaySearchWaiter() {
		waiterDialog = new MaterialDialog.Builder(getActivity())
				.progress(true, 1)
				.show();
	}

	private void dismissSearchWaiter() {
		if (waiterDialog != null) {
			waiterDialog.dismiss();
			waiterDialog = null;
		}
	}

	public void search(String searchString) {
		getSmartSpace().postSearchRequest(UserPref.getRadius(pref), searchString);
		displaySearchWaiter();
		EventBus.getDefault().post(new HistoryAppendEvent(searchString));

		searchMenuItem.collapseActionView();
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
				dialogAnchorContainer.setX(view.getX());
				dialogAnchorContainer.setY(view.getY());

				AddPointFragment.addToFragmentManager(
						AddPointFragment.newInstance(point),
						getFragmentManager(),
						R.id.dialog_anchor_container
				);
			}
		}
	}
}
