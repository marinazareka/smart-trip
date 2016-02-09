package org.fruct.oss.tsp.fragments.pickers;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import de.greenrobot.event.EventBus;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	public static TimePickerFragment newInstance(LocalTime localTime,
												 @Nullable LocalTime minTime, @Nullable LocalTime maxTime) {
		Bundle args = new Bundle();
		args.putSerializable("time", localTime);
		args.putSerializable("maxTime", maxTime);
		args.putSerializable("minTime", minTime);

		TimePickerFragment timePickerFragment = new TimePickerFragment();
		timePickerFragment.setArguments(args);
		return timePickerFragment;
	}

	private LocalTime maxTime;
	private LocalTime minTime;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LocalTime time;

		if (getArguments() == null) {
			time = LocalTime.now();
		} else {
			minTime = (LocalTime) getArguments().getSerializable("minTime");
			maxTime = (LocalTime) getArguments().getSerializable("maxTime");
			time = (LocalTime) getArguments().getSerializable("time");
			assert time != null;
		}

		return new TimePickerDialog(getActivity(),
				this, time.getHourOfDay(), time.getMinuteOfHour(), true);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		LocalTime localTime = new LocalTime(hourOfDay, minute);

		if (minTime != null && localTime.isBefore(minTime)) {
			localTime = minTime;
		}

		if (maxTime != null && localTime.isAfter(maxTime)) {
			localTime = maxTime;
		}

		EventBus.getDefault().post(new TimePickedEvent(localTime));
	}

	public static class TimePickedEvent {
		private LocalTime localTime;

		public TimePickedEvent(LocalTime localTime) {
			this.localTime = localTime;
		}

		public LocalTime getLocalTime() {
			return localTime;
		}
	}
}
