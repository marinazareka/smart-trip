package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.layers.PointsLayer;
import org.fruct.oss.tsp.layers.UserLayer;
import org.fruct.oss.tsp.util.Utils;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.overlay.Polyline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MapFragment extends BaseFragment {
	private static final Logger log = LoggerFactory.getLogger(MapFragment.class);

	@Bind(R.id.map_view)
	MapView mapView;

	@Bind(R.id.dialog_anchor_container)
	View dialogAnchorContainer;

	private TileCache tileCache;

	private TileDownloadLayer layer;
	private PointsLayer pointsLayer;
	private Polyline pathLayer;
	private UserLayer userLayer;

	private Subscription movementsSubscription;
	private Subscription tappedPointsSubscription;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.map, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		//menu.findItem(R.id.action_search).setVisible(false);
		//menu.findItem(R.id.action_schedule).setVisible(geoViewModel.isAnythingChecked());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_clear_search:
			getSearchStore().clear();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map, container, false);
		ButterKnife.bind(this, view);

		this.mapView.setClickable(true);
		this.mapView.getMapScaleBar().setVisible(false);
		this.mapView.setBuiltInZoomControls(false);
		this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
		this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

		this.mapView.getModel().displayModel.setFixedTileSize(256);

		// create a tile cache of suitable size
		this.tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache",
				mapView.getModel().displayModel.getTileSize(), 1f,
				mapView.getModel().frameBufferModel.getOverdrawFactor(), true);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		mapView.getModel().mapViewPosition.setCenter(new LatLong(61.78, 34.35));
		mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

		// tile renderer layer using internal render theme
		layer = new TileDownloadLayer(tileCache,
				mapView.getModel().mapViewPosition,
				OpenStreetMapMapnik.INSTANCE,
				AndroidGraphicFactory.INSTANCE);

		pointsLayer = new PointsLayer(getContext(), getDatabase(), getSearchStore());

		pathLayer = new Polyline(Utils.createPaint(
				AndroidGraphicFactory.INSTANCE.createColor(200, 100, 100, 255), Utils.getDP(4), Style.STROKE),
				AndroidGraphicFactory.INSTANCE);

		userLayer = new UserLayer(getContext());

		mapView.getLayerManager().getLayers().add(layer);
		mapView.getLayerManager().getLayers().add(pointsLayer);
		mapView.getLayerManager().getLayers().add(pathLayer);
		mapView.getLayerManager().getLayers().add(userLayer);
	}

	@Override
	public void onResume() {
		super.onResume();

		movementsSubscription = getScheduleStore().getObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Movement>>() {
					@Override
					public void call(List<Movement> movements) {
						updatePath(movements);
					}
				});

		tappedPointsSubscription = pointsLayer.getTappedPointsObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<PointsLayer.TapResult>() {
					@Override
					public void call(PointsLayer.TapResult tapResult) {
						onTappedPoints(tapResult);
					}
				});

		this.layer.onResume();
	}

	private void onTappedPoints(final PointsLayer.TapResult tapResult) {
		final List<Point> points = tapResult.tappedPoints;

		if (points.size() > 1) {
			String[] pointsTitles = new String[points.size()];

			for (int i = 0; i < pointsTitles.length; i++) {
				pointsTitles[i] = points.get(i).getTitle();
			}

			new MaterialDialog.Builder(getContext())
					.items(pointsTitles)
					.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
						@Override
						public boolean onSelection(MaterialDialog materialDialog, View view,
												   int index, CharSequence charSequence) {
							onTappedPoint(points.get(index), tapResult.x, tapResult.y);
							materialDialog.dismiss();
							return true;
						}
					})
					.show();
		} else {
			onTappedPoint(points.get(0), tapResult.x, tapResult.y);
		}
	}

	private void onTappedPoint(Point point, double x, double y) {
		if (!point.isPersisted()) {
			dialogAnchorContainer.setX((float) x);
			dialogAnchorContainer.setY((float) y);

			AddPointFragment.addToFragmentManager(
					AddPointFragment.newInstance(point),
					getFragmentManager(),
					R.id.dialog_anchor_container);
		}
	}

	@Override
	public void onPause() {
		this.layer.onPause();

		tappedPointsSubscription.unsubscribe();
		movementsSubscription.unsubscribe();

		super.onPause();
	}

	private void updatePath(List<Movement> movements) {
		List<LatLong> pathLatLong = new ArrayList<>(movements.size());

		if (movements.size() > 0) {
			Movement movement1 = movements.get(0);
			pathLatLong.add(new LatLong(movement1.getA().getLat(), movement1.getA().getLon()));
		}

		for (Movement movement : movements) {
			pathLatLong.add(new LatLong(movement.getB().getLat(), movement.getB().getLon()));
		}

		pathLayer.getLatLongs().clear();
		pathLayer.getLatLongs().addAll(pathLatLong);

		pathLayer.requestRedraw();
	}

	@Override
	public void onStop() {
		mapView.getLayerManager().getLayers().remove(userLayer);
		mapView.getLayerManager().getLayers().remove(layer);
		mapView.getLayerManager().getLayers().remove(pointsLayer);

		layer.onDestroy();
		pointsLayer.onDestroy();
		pathLayer.onDestroy();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		this.mapView.destroyAll();
		super.onDestroy();
	}
}
