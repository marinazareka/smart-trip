package org.fruct.oss.tsp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.util.UserPref;
import org.fruct.oss.tsp.util.Utils;
import org.joda.time.LocalDate;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class AddScheduleFragment extends DialogFragment {
	private static final String TAG_DATE_PICKER_FRAGMENT = "TAG_DATE_PICKER_FRAGMENT";

	public static AddScheduleFragment newInstance(@Nullable Schedule schedule) {
		Bundle args = new Bundle();
		if (schedule != null) {
			args.putString("title", schedule.getTitle());
			args.putSerializable("tspType", schedule.getTspType());

			args.putString("roadType", schedule.getRoadType());
			args.putSerializable("startDate", schedule.getStartDate());
			args.putSerializable("endDate", schedule.getEndDate());
		}

		AddScheduleFragment fragment = new AddScheduleFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Bind(R.id.tsp_type_spinner)
	Spinner tspTypeSpinner;

	@Bind(R.id.road_type_spinner)
	Spinner roadTypeSpinner;

	@Bind(R.id.title_edit_text)
	EditText editText;

	@Bind(R.id.start_interval_text)
	TextView startIntervalText;

	@Bind(R.id.end_interval_text)
	TextView endIntervalText;

	private DatePickerMode datePickerMode;
	private LocalDate startDate;
	private LocalDate endDate;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();

		Bundle args = getArguments();
		TspType tspType = (TspType) args.getSerializable("tspType");
		String roadType = args.getString("roadType");
		String title = args.getString("title");
		startDate = (LocalDate) args.getSerializable("startDate");
		if (startDate == null) {
			startDate = LocalDate.now();
		}

		endDate = (LocalDate) args.getSerializable("endDate");
		if (endDate == null) {
			endDate = LocalDate.now();
		}

		final MaterialDialog dialog = new MaterialDialog.Builder(context)
				.title(R.string.title_new_schedule)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.customView(R.layout.dialog_new_schedule, false)
				.onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
						dialog.dismiss();
					}
				})
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
						String title = editText.getText().toString();
						TspType tspType = tspTypeSpinner.getSelectedItemPosition() == 0
								? TspType.OPEN : TspType.CLOSED;
						String roadType = "foot";
						switch (roadTypeSpinner.getSelectedItemPosition()) {
						case 0:
							roadType = "car";
							break;
						case 2:
							roadType = "bus";
							break;
						}

						if (!TextUtils.isEmpty(title)) {
							EventBus.getDefault().post(new ScheduleDialogFinishedEvent(
									new Schedule(title, tspType, roadType, startDate, endDate)
							));

							dialog.dismiss();
						}
					}
				})
				.autoDismiss(false)
				.build();

		ButterKnife.bind(this, dialog);

		// Setup tsp type spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
				R.array.tsp_types_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tspTypeSpinner.setAdapter(adapter);

		// Initial tsp type
		if (tspType == null) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			tspType = UserPref.isClosedRoute(pref) ? TspType.CLOSED : TspType.OPEN;
		}

		tspTypeSpinner.setSelection(tspType == TspType.OPEN ? 0 : 1);

		// Setup road type spinner
		ArrayAdapter<CharSequence> roadAdapter = ArrayAdapter.createFromResource(context,
				R.array.road_types_array, android.R.layout.simple_spinner_item);
		roadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roadTypeSpinner.setAdapter(roadAdapter);

		// Initial road type
		if (roadType != null) {
			int idx = 0;
			switch (roadType) {
			case "car":
				idx = 0;
				break;
			case "foot":
				idx = 1;
				break;
			case "bus":
				idx = 2;
				break;
			}
			roadTypeSpinner.setSelection(idx);
		}

		// Initial title
		if (!TextUtils.isEmpty(title)) {
			((EditText) ButterKnife.findById(dialog, R.id.title_edit_text)).setText(title);
		}

		// Initial dates
		updateDateViews();

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				EventBus.getDefault().post(new DismissedEvent());
			}
		});

		return dialog;
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

	@OnClick(R.id.start_interval_text)
	void onStartIntervalClicked() {
		DatePickerFragment fragment = DatePickerFragment.newInstance(startDate, null, endDate);
		fragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
		datePickerMode = DatePickerMode.START_DATE;
	}

	@OnClick(R.id.end_interval_text)
	void onEndIntervalClicked() {
		DatePickerFragment fragment = DatePickerFragment.newInstance(endDate, startDate, null);
		fragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
		datePickerMode = DatePickerMode.END_DATE;
	}

	public void onEventMainThread(DatePickerFragment.DatePickedEvent event) {
		if (datePickerMode == null) {
			return;
		}

		switch (datePickerMode) {
		case START_DATE:
			startDate = event.getDate();
			break;

		case END_DATE:
			endDate = event.getDate();
			break;

		}

		datePickerMode = null;
		updateDateViews();
	}

	private void updateDateViews() {
		if (startDate != null) {
			startIntervalText.setText(Utils.localDateToString(startDate));
		}

		if (endDate != null) {
			endIntervalText.setText(Utils.localDateToString(endDate));
		}
	}

	public static class ScheduleDialogFinishedEvent {
		private Schedule newSchedule;

		public ScheduleDialogFinishedEvent(Schedule newSchedule) {
			this.newSchedule = newSchedule;
		}

		public Schedule getNewSchedule() {
			return newSchedule;
		}
	}

	public static class DismissedEvent {
	}

	private enum DatePickerMode {
		START_DATE, END_DATE,
	}
}
