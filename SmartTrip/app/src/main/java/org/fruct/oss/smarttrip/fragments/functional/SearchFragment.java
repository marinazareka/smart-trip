package org.fruct.oss.smarttrip.fragments.functional;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.smarttrip.App;
import org.fruct.oss.smarttrip.EventReceiver;
import org.fruct.oss.smarttrip.R;
import org.fruct.oss.smarttrip.events.LocationEvent;
import org.fruct.oss.smarttrip.points.PointsJob;
import org.fruct.oss.smarttrip.points.SmartPointsLoader;

import de.greenrobot.event.EventBus;

public class SearchFragment extends Fragment {
	private Location location;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		EventBus.getDefault().registerSticky(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			actionSearch();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void actionSearch() {
		if (location == null) {
			return;
		}

		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.title("Find nearest")
				.customView(R.layout.dialog_search, true)
				.positiveText("Search")
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						EditText radiusInput = (EditText) dialog.getCustomView().findViewById(R.id.radiusEditText);
						EditText patternInput = (EditText) dialog.getCustomView().findViewById(R.id.patternEditText);

						double radius = Integer.parseInt(radiusInput.getText().toString());
						String pattern = patternInput.getText().toString();

						Toast.makeText(getActivity(), "Searching...", Toast.LENGTH_SHORT).show();

						App.getJobManager().addJobInBackground(new PointsJob(new SmartPointsLoader(),
								location.getLatitude(), location.getLongitude(), radius, pattern));
					}
				}).build();

		final View actionButton = dialog.getActionButton(DialogAction.POSITIVE);
		final EditText radiusInput = (EditText) dialog.getCustomView().findViewById(R.id.radiusEditText);

		radiusInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int ignored = Integer.parseInt(s.toString());
					if (ignored <= 0)
						throw new NumberFormatException("Radius MUST be positive");

					actionButton.setEnabled(true);
				} catch (NumberFormatException ex) {
					actionButton.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		dialog.show();
	}

	@EventReceiver
	public void onEventMainThread(LocationEvent locationEvent) {
		this.location = locationEvent.getLocation();
	}
}
