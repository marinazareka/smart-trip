package org.fruct.oss.tsp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.fruct.oss.tsp.R;
import org.fruct.oss.tsp.layers.PointsLayer;
import org.fruct.oss.tsp.viewmodel.DefaultGeoViewModel;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;

public class MapFragment extends BaseFragment {
	private MapView mapView;
	private TileCache tileCache;

	private TileDownloadLayer layer;
	private PointsLayer pointsLayer;

	private DefaultGeoViewModel geoViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		geoViewModel = new DefaultGeoViewModel(getActivity(), getGeoStore());

		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_point_list_fragment, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_search).setVisible(false);
		//menu.findItem(R.id.action_schedule).setVisible(geoViewModel.isAnythingChecked());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_schedule:
			scheduleSelection(geoViewModel);
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getContext());

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

		return mapView;
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

		pointsLayer = new PointsLayer(getContext(), geoViewModel);

		mapView.getLayerManager().getLayers().add(layer);
		mapView.getLayerManager().getLayers().add(pointsLayer);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.layer.onResume();
		this.geoViewModel.start();
	}

	@Override
	public void onPause() {
		this.layer.onPause();
		this.geoViewModel.stop();
		super.onPause();
	}

	@Override
	public void onStop() {
		this.mapView.getLayerManager().getLayers().remove(layer);
		this.mapView.getLayerManager().getLayers().remove(pointsLayer);

		this.layer.onDestroy();
		this.pointsLayer.onDestroy();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		this.mapView.destroyAll();
		super.onDestroy();
	}
}
