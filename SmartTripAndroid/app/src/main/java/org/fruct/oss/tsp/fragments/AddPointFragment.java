package org.fruct.oss.tsp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.activities.MainActivity;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.fragments.root.MapFragment;
import org.fruct.oss.tsp.util.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

public class AddPointFragment extends BaseFragment {
	private static final Logger log = LoggerFactory.getLogger(AddPointFragment.class);

	public static final String BACK_STACK_TAG = "AddPointFragment_TAG";

	private static final String TAG_ADD_SCHEDULE_FRAGMENT = "TAG_ADD_SCHEDULE_FRAGMENT";

	public static void addToFragmentManager(AddPointFragment fragment, FragmentManager fragmentManager,
											@IdRes int container) {
		fragmentManager
				.beginTransaction()
				.addToBackStack(BACK_STACK_TAG)
				.add(container, fragment, "add-point-fragment")
				.commit();
	}

	public static AddPointFragment newInstance(Point point) {
		Bundle args = new Bundle();
		Point.save(point, args, "point");

		AddPointFragment fragment = new AddPointFragment();
		fragment.setArguments(args);
		return fragment;
	}

	private Point point;
	private SharedPreferences pref;
	private boolean isPopupMenuItemSelected;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		point = Point.restore(getArguments(), "point");
		pref = PreferenceManager.getDefaultSharedPreferences(getContext());

		Toast.makeText(getContext(), point.getId(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.empty_view, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		PopupMenu popupMenu = new PopupMenu(getContext(), getView());
		popupMenu.inflate(R.menu.point);

		popupMenu.getMenu().findItem(R.id.action_add_current_schedule)
				.setVisible(!point.isPersisted() && Pref.hasCurrentSchedule(pref));
		popupMenu.getMenu().findItem(R.id.action_add_new_schedule)
				.setVisible(!point.isPersisted());
		popupMenu.getMenu().findItem(R.id.action_delete_from_schedule)
				.setVisible(point.isPersisted());
		popupMenu.getMenu().findItem(R.id.action_map)
				.setVisible(!(getFragmentManager().findFragmentById(R.id.container) instanceof MapFragment));

		popupMenu.setOnMenuItemClickListener(new PointMenuListener());
		popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {
				if (!isPopupMenuItemSelected) {
					dismissFragment();
				}
			}
		});

		popupMenu.show();


//		if (Pref.hasCurrentSchedule(pref)) {
//			PopupMenu popupMenu = new PopupMenu(getContext(), getView());
//			popupMenu.inflate(R.menu.point);
//			popupMenu.setOnMenuItemClickListener(new PointMenuListener());
//			popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
//				@Override
//				public void onDismiss(PopupMenu menu) {
//					if (!isPopupMenuItemSelected) {
//						dismissFragment();
//					}
//				}
//			});
//			popupMenu.show();
//		} else {
//			onPointAddToNewSchedule(point);
//		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	private void dismissFragment() {
		getFragmentManager().
				popBackStack(BACK_STACK_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	private class PointMenuListener implements PopupMenu.OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			isPopupMenuItemSelected = true;
			switch (item.getItemId()) {
			case R.id.action_add_current_schedule:
				onPointAddToCurrentSchedule(point);
				dismissFragment();
				break;

			case R.id.action_add_new_schedule:
				onPointAddToNewSchedule(point);
				break;

			case R.id.action_map:
				MainActivity mainActivity = (MainActivity) getActivity();
				mainActivity.switchFragment(MapFragment.newInstance(point));
				break;

			default:
				return false;
			}

			return true;
		}
	}

	private void onPointAddToCurrentSchedule(Point point) {
		long currentScheduleId = Pref.getCurrentSchedule(pref);
		if (currentScheduleId != 0) {
			getDatabase().insertPoint(currentScheduleId, point);
			getSearchStore().removePoint(point);

		}
	}

	private void onPointAddToNewSchedule(Point point) {
		AddScheduleFragment addScheduleFragment = AddScheduleFragment.newInstance(null);
		addScheduleFragment.show(getFragmentManager(), TAG_ADD_SCHEDULE_FRAGMENT);
	}

	public void onEventMainThread(AddScheduleFragment.ScheduleDialogFinishedEvent event) {
		onNewScheduleDialogFinished(event.getNewSchedule());
	}

	public void onEventMainThread(AddScheduleFragment.DismissedEvent event) {
		dismissFragment();
	}

	private void onNewScheduleDialogFinished(Schedule newSchedule) {
		log.debug("New schedule {} {}", newSchedule.getTitle());
		long insertedId = getDatabase().insertSchedule(newSchedule);
		Pref.setCurrentSchedule(pref, insertedId);
		getDatabase().setCurrentSchedule(insertedId);
		onPointAddToCurrentSchedule(point);
	}
}
