package org.fruct.oss.smarttrip;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.fruct.oss.smarttrip.events.LocationEvent;
import org.fruct.oss.smarttrip.fragments.MapFragment;
import org.fruct.oss.smarttrip.fragments.PlaceHolderFragment;
import org.fruct.oss.smarttrip.location.GoogleLocationUpdater;
import org.fruct.oss.smarttrip.location.LocationListener;
import org.fruct.oss.smarttrip.location.LocationUpdater;
import org.fruct.oss.smarttrip.points.PointsJob;
import org.fruct.oss.smarttrip.points.SmartPointsLoader;
import org.fruct.oss.smarttrip.points.TestPointsLoader;
import org.fruct.oss.smarttrip.util.Test;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
	private static int REQUEST_RESOLVE_GOOGLE_ERROR = 101;

	private static String STATE_RESOLVING_ERROR = "state_resolving_error";

	private GoogleApiClient googleClient;
	private boolean isResolvingGoogleClientError;

	private LocationUpdater locationUpdater;
	private Location lastLocation;
	private Drawer drawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawer = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withHeader(R.layout.drawer_header)
				.withFireOnInitialOnClick(true)
				.addDrawerItems(
						new PrimaryDrawerItem().withName("Map").withIcon(R.drawable.ic_nav_map),
						new PrimaryDrawerItem().withName("Goodbye world")
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
						Fragment fragment;

						switch (i) {
						case 0:
							fragment = MapFragment.newInstance();
							break;

						default:
							fragment = null;
							break;
						}

						if (fragment != null) {
							getSupportFragmentManager()
									.beginTransaction()
									.replace(R.id.content, fragment)
									.commit();
						} else {
							finish();
						}
						return false;
					}
				})
				.build();

		if (savedInstanceState != null) {
			isResolvingGoogleClientError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
		}

		setupGoogleClient();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
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
		if (lastLocation == null) {
			return;
		}

		MaterialDialog dialog = new MaterialDialog.Builder(this)
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

						Toast.makeText(MainActivity.this, "Searching...", Toast.LENGTH_SHORT).show();

						App.getJobManager().addJobInBackground(new PointsJob(new SmartPointsLoader(),
									lastLocation.getLatitude(), lastLocation.getLongitude(), radius, pattern));
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

	private void setupGoogleClient() {
		googleClient = new GoogleApiClient.Builder(this)
				.addApiIfAvailable(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!isResolvingGoogleClientError) {
			googleClient.connect();
		}
	}

	@Override
	protected void onStop() {
		if (locationUpdater != null) {
			locationUpdater.stop();
		}

		googleClient.disconnect();

		super.onStop();
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (googleClient.hasConnectedApi(LocationServices.API)) {
			locationUpdater = new GoogleLocationUpdater(googleClient);
			locationUpdater.setListener(this);
			locationUpdater.start();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		if (locationUpdater != null) {
			locationUpdater.stop();
			locationUpdater = null;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (isResolvingGoogleClientError) {
			return;
		}

		if (connectionResult.hasResolution()) {
			try {
				isResolvingGoogleClientError = true;
				connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_GOOGLE_ERROR);
			} catch (IntentSender.SendIntentException e) {
				googleClient.connect();
			}
		} else {
			isResolvingGoogleClientError = true;
			String errorString = GooglePlayServicesUtil.getErrorString(connectionResult.getErrorCode());
			Toast.makeText(this, "Can't connect google client: " + errorString,
					Toast.LENGTH_SHORT).show();

			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	private void showErrorDialog(int errorCode) {
		// Create a fragment for the error dialog
		ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
		// Pass the error that should be displayed
		Bundle args = new Bundle();
		args.putInt("dialog_error", errorCode);
		dialogFragment.setArguments(args);
		dialogFragment.show(getSupportFragmentManager(), "errordialog");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_RESOLVE_GOOGLE_ERROR) {
			isResolvingGoogleClientError = false;
			if (resultCode == RESULT_OK) {
				if (!googleClient.isConnecting() || !googleClient.isConnected()) {
					googleClient.connect();
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(STATE_RESOLVING_ERROR, isResolvingGoogleClientError);
	}

	@Override
	public void onNewLocation(Location location) {
		lastLocation = location;

		EventBus.getDefault().postSticky(new LocationEvent(location));
	}

	public static class ErrorDialogFragment extends DialogFragment {
		public ErrorDialogFragment() { }

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Get the error code and retrieve the appropriate dialog
			int errorCode = this.getArguments().getInt("dialog_error");
			return GooglePlayServicesUtil.getErrorDialog(errorCode,
					this.getActivity(), REQUEST_RESOLVE_GOOGLE_ERROR);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (isAdded()) {
				((MainActivity) getActivity()).onDialogDismissed();
			}
		}
	}

	private void onDialogDismissed() {
		isResolvingGoogleClientError = false;
	}

}
