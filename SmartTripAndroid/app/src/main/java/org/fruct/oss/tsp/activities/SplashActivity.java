package org.fruct.oss.tsp.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.fruct.oss.tsp.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Начальный экран приложение
 *
 * Проверяет доступность сервисов Google Play и при необходимости выполняет их обновление
 * При доступности запускает {@link MainActivity}
 */
public class SplashActivity extends Activity {
	private static final Logger log = LoggerFactory.getLogger(SplashActivity.class);

	private static final int REQUEST_CODE = 0;
	private boolean isResolving;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void onResume() {
		super.onResume();

		GoogleApiAvailability availability = GoogleApiAvailability.getInstance();

		int availabilityCode = availability.isGooglePlayServicesAvailable(this);
		if (!isResolving) {
			if (availabilityCode != ConnectionResult.SUCCESS) {
				log.warn("Google play services unavailable");
				if (availability.isUserResolvableError(availabilityCode)) {
					Dialog errorDialog = availability.getErrorDialog(this,
							availabilityCode, REQUEST_CODE, new DialogCancelledCallback());
					isResolving = true;
					errorDialog.show();
				} else {
					log.warn("Google play services error unresolvable");
				}
			} else {
				log.info("Google play services available");
				startMainActivity();
			}
		}
	}

	private void startMainActivity() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			if (resultCode != RESULT_OK) {
				log.warn("Can't resolve Play Services error");
			}
			isResolving = false;
			startMainActivity();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private class DialogCancelledCallback implements DialogInterface.OnCancelListener {
		@Override
		public void onCancel(DialogInterface dialog) {
			isResolving = false;
			startMainActivity();
		}
	}
}
