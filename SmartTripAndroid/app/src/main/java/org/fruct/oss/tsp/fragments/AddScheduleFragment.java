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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.util.UserPref;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class AddScheduleFragment extends DialogFragment {
	public static AddScheduleFragment newInstance(Context context,
												  @Nullable String title, @Nullable TspType tspType) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putSerializable("tspType", tspType);

		AddScheduleFragment fragment = new AddScheduleFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();

		Bundle args = getArguments();
		TspType tspType = (TspType) args.getSerializable("tspType");
		String title = args.getString("title");

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
						Spinner spinner = ButterKnife.findById(dialog, R.id.tsp_type_spinner);
						Spinner roadSpinner = ButterKnife.findById(dialog, R.id.road_type_spinner);
						EditText editText = ButterKnife.findById(dialog, R.id.title_edit_text);

						String title = editText.getText().toString();
						TspType tspType = spinner.getSelectedItemPosition() == 0
								? TspType.OPEN : TspType.CLOSED;
						String roadType = "foot";
						switch (spinner.getSelectedItemPosition()) {
						case 0:
							roadType = "car";
							break;
						case 2:
							roadType = "bus";
							break;
						}

						if (!TextUtils.isEmpty(title)) {
							EventBus.getDefault().post(new ScheduleDialogFinishedEvent(title, tspType, roadType));
							dialog.dismiss();
						}
					}
				})
				.autoDismiss(false)
				.build();

		// Setup tsp type spinner
		Spinner spinner = ButterKnife.findById(dialog, R.id.tsp_type_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
				R.array.tsp_types_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		// Initial tsp type
		if (tspType == null) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			tspType = UserPref.isClosedRoute(pref) ? TspType.CLOSED : TspType.OPEN;
		}

		spinner.setSelection(tspType == TspType.OPEN ? 0 : 1);

		// Setup road type spinner
		Spinner roadSpinner = ButterKnife.findById(dialog, R.id.road_type_spinner);
		ArrayAdapter<CharSequence> roadAdapter = ArrayAdapter.createFromResource(context,
				R.array.road_types_array, android.R.layout.simple_spinner_item);
		roadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roadSpinner.setAdapter(roadAdapter);

		// Initial title
		if (!TextUtils.isEmpty(title)) {
			((EditText) ButterKnife.findById(dialog, R.id.title_edit_text)).setText(title);
		}

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				EventBus.getDefault().post(new DismissedEvent());
			}
		});

		return dialog;
	}

	public static class ScheduleDialogFinishedEvent {
		private final String title;
		private final TspType tspType;
		private final String roadType;

		public ScheduleDialogFinishedEvent(String title, TspType tspType, String roadType) {

			this.title = title;
			this.tspType = tspType;
			this.roadType = roadType;
		}

		public String getTitle() {
			return title;
		}

		public TspType getTspType() {
			return tspType;
		}

		public String getRoadType() {
			return roadType;
		}
	}

	public static class DismissedEvent {
	}
}
