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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

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

import de.greenrobot.event.EventBus;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
	private static int REQUEST_RESOLVE_GOOGLE_ERROR = 101;

	private static String STATE_RESOLVING_ERROR = "state_resolving_error";

	private GoogleApiClient googleClient;
	private boolean isResolvingGoogleClientError;

	private LocationUpdater locationUpdater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Drawer drawer = new DrawerBuilder()
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
