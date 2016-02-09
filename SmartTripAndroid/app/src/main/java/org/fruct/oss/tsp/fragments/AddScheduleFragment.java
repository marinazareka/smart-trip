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
import org.fruct.oss.tsp.fragments.pickers.DatePickerFragment;
import org.fruct.oss.tsp.fragments.pickers.TimePickerFragment;
import org.fruct.oss.tsp.util.UserPref;
import org.fruct.oss.tsp.util.Utils;
import org.joda.time.LocalDateTime;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class AddScheduleFragment extends DialogFragment {
	private static final String TAG_DATE_PICKER_FRAGMENT = "TAG_DATE_PICKER_FRAGMENT";
	private static final String TAG_TIME_PICKER_FRAGMENT = "TAG_TIME_PICKER_FRAGMENT";

	public static AddScheduleFragment newInstance(@Nullable Schedule schedule) {
		Bundle args = new Bundle();
		if (schedule != null) {
			args.putString("title", schedule.getTitle());
			args.putSerializable("tspType", schedule.getTspType());

			args.putString("roadType", schedule.getRoadType());
			args.putSerializable("startDateTime", schedule.getStartDateTime());
			args.putSerializable("endDateTime", schedule.getEndDateTime());
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

	@Bind(R.id.start_interval_date_text)
	TextView startIntervalDateText;

	@Bind(R.id.end_interval_date_text)
	TextView endIntervalDateText;

	@Bind(R.id.start_interval_time_text)
	TextView startIntervalTimeText;

	@Bind(R.id.end_interval_time_text)
	TextView endIntervalTimeText;

	private IntervalPickerMode intervalPickerMode;

	private LocalDateTime startDateTime;

	private LocalDateTime endDateTime;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();

		Bundle args = getArguments();
		TspType tspType = (TspType) args.getSerializable("tspType");
		String roadType = args.getString("roadType");
		String title = args.getString("title");
		startDateTime = (LocalDateTime) args.getSerializable("startDateTime");
		if (startDateTime == null) {
			startDateTime = LocalDateTime.now();
		}

		endDateTime = (LocalDateTime) args.getSerializable("endDateTime");
		if (endDateTime == null) {
			endDateTime = LocalDateTime.now().plusDays(1);
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
									new Schedule(title, tspType, roadType, startDateTime, endDateTime)
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

	@OnClick(R.id.start_interval_date_text)
	void onStartIntervalClicked() {
		DatePickerFragment fragment = DatePickerFragment.newInstance(startDateTime.toLocalDate(),
				null, endDateTime.toLocalDate());
		fragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
		intervalPickerMode = IntervalPickerMode.START_DATE;
	}

	@OnClick(R.id.end_interval_date_text)
	void onEndIntervalClicked() {
		DatePickerFragment fragment = DatePickerFragment.newInstance(endDateTime.toLocalDate(),
				startDateTime.toLocalDate(), null);
		fragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
		intervalPickerMode = IntervalPickerMode.END_DATE;
	}

	@OnClick(R.id.start_interval_time_text)
	void onStartIntervalTimeClicked() {
		TimePickerFragment fragment = TimePickerFragment.newInstance(startDateTime.toLocalTime(),
				null, endDateTime.toLocalTime());
		fragment.show(getFragmentManager(), TAG_TIME_PICKER_FRAGMENT);
		intervalPickerMode = IntervalPickerMode.START_DATE;
	}

	@OnClick(R.id.end_interval_time_text)
	void onEndIntervalTimeClicked() {
		TimePickerFragment fragment = TimePickerFragment.newInstance(endDateTime.toLocalTime(),
				startDateTime.toLocalTime(), null);
		fragment.show(getFragmentManager(), TAG_TIME_PICKER_FRAGMENT);
		intervalPickerMode = IntervalPickerMode.END_DATE;
	}

	public void onEventMainThread(DatePickerFragment.DatePickedEvent event) {
		if (intervalPickerMode == null) {
			return;
		}

		switch (intervalPickerMode) {
		case START_DATE:
			startDateTime = event.getDate().toLocalDateTime(startDateTime.toLocalTime());
			break;

		case END_DATE:
			endDateTime = event.getDate().toLocalDateTime(endDateTime.toLocalTime());
			break;
		}

		intervalPickerMode = null;
		updateDateViews();
	}

	public void onEventMainThread(TimePickerFragment.TimePickedEvent event) {
		if (intervalPickerMode == null) {
			return;
		}

		switch (intervalPickerMode) {
		case START_DATE:
			startDateTime = startDateTime.toLocalDate().toLocalDateTime(event.getLocalTime());
			break;

		case END_DATE:
			endDateTime = startDateTime.toLocalDate().toLocalDateTime(event.getLocalTime());
			break;
		}

		intervalPickerMode = null;
		updateDateViews();
	}

	private void updateDateViews() {
		if (startDateTime != null) {
			startIntervalTimeText.setText(Utils.partialToString(startDateTime.toLocalTime()));
			startIntervalDateText.setText(Utils.partialToString(startDateTime.toLocalDate()));
		}

		if (endDateTime != null) {
			endIntervalTimeText.setText(Utils.partialToString(endDateTime.toLocalTime()));
			endIntervalDateText.setText(Utils.partialToString(endDateTime.toLocalDate()));
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

	private enum IntervalPickerMode {
		START_DATE, END_DATE,
	}
}
