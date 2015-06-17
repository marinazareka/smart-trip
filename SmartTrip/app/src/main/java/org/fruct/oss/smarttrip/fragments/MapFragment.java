package org.fruct.oss.smarttrip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;

public class MapFragment extends Fragment {
	private MapView mapView;
	private TileCache tileCache;
	private TileDownloadLayer layer;

	public static MapFragment newInstance() {
		return new MapFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getActivity());

		this.mapView.setClickable(true);
		this.mapView.getMapScaleBar().setVisible(true);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
		this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

		this.mapView.getModel().displayModel.setFixedTileSize(256);

		// create a tile cache of suitable size
		this.tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache",
				mapView.getModel().displayModel.getTileSize(), 1f,
				this.mapView.getModel().frameBufferModel.getOverdrawFactor());

		return mapView;
	}

	@Override
	public void onStart() {
		super.onStart();

		this.mapView.getModel().mapViewPosition.setCenter(new LatLong(61.78, 34.35));
		this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

		// tile renderer layer using internal render theme
		this.layer = new TileDownloadLayer(tileCache,
				mapView.getModel().mapViewPosition,
				OpenStreetMapMapnik.INSTANCE,
				AndroidGraphicFactory.INSTANCE);

		// only once a layer is associated with a mapView the rendering starts
		this.mapView.getLayerManager().getLayers().add(layer);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.layer.onResume();
	}

	@Override
	public void onPause() {
		this.layer.onPause();

		super.onPause();
	}

	@Override
	public void onStop() {
		this.mapView.getLayerManager().getLayers().remove(layer);
		this.layer.onDestroy();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		this.tileCache.destroy();
		this.mapView.getModel().mapViewPosition.destroy();
		this.mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();

		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
