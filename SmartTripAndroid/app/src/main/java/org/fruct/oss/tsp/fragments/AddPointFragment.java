package org.fruct.oss.tsp.fragments;

import android.content.DialogInterface;
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

import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.util.EditScheduleDialog;
import org.fruct.oss.tsp.util.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddPointFragment extends BaseFragment {
	private static final Logger log = LoggerFactory.getLogger(AddPointFragment.class);

	public static final String BACK_STACK_TAG = "AddPointFragment_TAG";


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
		args.putParcelable("point", point);

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

		point = getArguments().getParcelable("point");
		pref = PreferenceManager.getDefaultSharedPreferences(getContext());
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

		if (Pref.hasCurrentSchedule(pref)) {
			PopupMenu popupMenu = new PopupMenu(getContext(), getView());
			popupMenu.inflate(R.menu.point);
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
		} else {
			onPointAddToNewSchedule(point);
		}
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
		} else {
			// TODO: notify user or disallow point adding without current schedule
		}

	}

	private void onPointAddToNewSchedule(Point point) {
		MaterialDialog dialog = EditScheduleDialog.create(getContext(), null, null,
				new EditScheduleDialog.Listener() {
					@Override
					public void onScheduleDialogFinished(String title, TspType tspType) {
						onNewScheduleDialogFinished(title, tspType);
					}
				});
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				dismissFragment();
			}
		});
		dialog.show();
	}

	private void onNewScheduleDialogFinished(String title, TspType tspType) {
		log.debug("New schedule {} {}", title, tspType);
		long insertedId = getDatabase().insertSchedule(new Schedule(title, tspType));
		Pref.setCurrentSchedule(pref, insertedId);
		getDatabase().setCurrentSchedule(insertedId);
		onPointAddToCurrentSchedule(point);
	}
}
