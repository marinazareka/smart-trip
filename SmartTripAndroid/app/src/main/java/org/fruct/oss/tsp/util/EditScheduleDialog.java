package org.fruct.oss.tsp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.TspType;

import butterknife.ButterKnife;

public class EditScheduleDialog {
	public static MaterialDialog create(Context context, @Nullable String title, @Nullable TspType tspType,
										@NonNull final Listener listener) {
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
						EditText editText = ButterKnife.findById(dialog, R.id.title_edit_text);

						String title = editText.getText().toString();
						TspType tspType = spinner.getSelectedItemPosition() == 0
								? TspType.OPEN : TspType.CLOSED;

						if (!TextUtils.isEmpty(title)) {
							listener.onScheduleDialogFinished(title, tspType);
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

		// Initial title
		if (!TextUtils.isEmpty(title)) {
			((EditText) ButterKnife.findById(dialog, R.id.title_edit_text)).setText(title);
		}

		return dialog;
	}

	public interface Listener {
		void onScheduleDialogFinished(String title, TspType tspType);
	}
}
