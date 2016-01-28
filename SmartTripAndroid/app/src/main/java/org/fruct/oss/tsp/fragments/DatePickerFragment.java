package org.fruct.oss.tsp.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.DatePicker;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import de.greenrobot.event.EventBus;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	public static DatePickerFragment newInstance(LocalDate localDate,
												 @Nullable LocalDate minDate,
												 @Nullable LocalDate maxDate) {
		Bundle args = new Bundle();
		args.putSerializable("date", localDate);
		args.putSerializable("minDate", minDate);
		args.putSerializable("maxDate", maxDate);

		DatePickerFragment datePickerFragment = new DatePickerFragment();
		datePickerFragment.setArguments(args);
		return datePickerFragment;
	}

	LocalDate minDate = null;
	LocalDate maxDate = null;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LocalDate date;
		if (getArguments() == null) {
			date = LocalDate.now();
		} else {
			date = (LocalDate) getArguments().getSerializable("date");
			minDate = (LocalDate) getArguments().getSerializable("minDate");
			maxDate = (LocalDate) getArguments().getSerializable("maxDate");
			assert date != null;
		}

		DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
				date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
		if (minDate != null) {
			LocalTime dayStart = new LocalTime(0, 0, 0);
			datePickerDialog.getDatePicker()
					.setMinDate(minDate.toDateTime(dayStart, DateTimeZone.getDefault()).getMillis());
		}
		if (maxDate != null) {
			LocalTime dayEnd = new LocalTime(23, 59, 59);
			datePickerDialog.getDatePicker()
					.setMaxDate(maxDate.toDateTime(dayEnd, DateTimeZone.getDefault()).getMillis());
		}

		return datePickerDialog;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);

		if (maxDate != null && date.isAfter(maxDate)) {
			date = maxDate;
		}

		if (minDate != null && date.isBefore(minDate)) {
			date = minDate;
		}

		EventBus.getDefault().post(new DatePickedEvent(date));
	}

	public static class DatePickedEvent {
		private LocalDate date;

		public DatePickedEvent(LocalDate date) {
			this.date = date;
		}

		public LocalDate getDate() {
			return date;
		}
	}
}
