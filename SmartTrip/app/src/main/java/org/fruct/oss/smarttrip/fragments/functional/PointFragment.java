package org.fruct.oss.smarttrip.fragments.functional;

import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.smarttrip.EventReceiver;
import org.fruct.oss.smarttrip.events.PointClickedEvent;
import org.fruct.oss.smarttrip.points.Point;

import de.greenrobot.event.EventBus;

public class PointFragment extends Fragment {
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

	@EventReceiver
	public void onEventMainThread(PointClickedEvent event) {
		Point point = event.getPoint();

		new MaterialDialog.Builder(getActivity())
				.title(point.getName())
				.show();
	}
}
