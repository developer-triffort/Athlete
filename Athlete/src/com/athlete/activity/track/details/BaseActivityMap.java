package com.athlete.activity.track.details;

import java.util.ArrayList;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.athlete.AthleteApplication;
import com.athlete.db.DatabaseHelper;
import com.athlete.google.android.apps.mytracks.MapOverlay;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils.Factory;
import com.athlete.google.android.apps.mytracks.content.Track;
import com.athlete.google.android.apps.mytracks.content.TrackDataHub;
import com.athlete.google.android.apps.mytracks.content.TrackDataListener;
import com.athlete.google.android.apps.mytracks.content.Waypoint;
import com.athlete.google.android.apps.mytracks.stats.TripStatistics;
import com.athlete.google.android.apps.mytracks.util.GeoRect;
import com.athlete.google.android.apps.mytracks.util.LocationUtils;
import com.athlete.util.OrmLiteBaseMapActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class BaseActivityMap extends OrmLiteBaseMapActivity<DatabaseHelper>
		implements TrackDataListener {

	
	// Google's latitude and longitude
	protected static final String CURRENT_LOCATION_KEY = "current_location_key";
	protected static final String KEEP_CURRENT_LOCATION_VISIBLE_KEY = "keep_current_location_visible_key";
	protected static final String ZOOM_TO_CURRENT_LOCATION_KEY = "zoom_to_current_location_key";
	protected static final String MAP_TYPE = "map_type";
	private static final double DEFAULT_LATITUDE = 37.423;
	private static final double DEFAULT_LONGITUDE = -122.084;
	protected static final float DEFAULT_ZOOM_LEVEL = 18f;
	private static final int MAP_VIEW_PADDING = 32;
	protected TrackDataHub trackDataHub;
	protected Location currentLocation;
	protected boolean keepCurrentLocationVisible;
	protected boolean zoomToCurrentLocation;
	protected OnLocationChangedListener onLocationChangedListener;
	protected long markerTrackId = -1L;
	protected long markerId = -1L;
	
	
	protected Track currentTrack;
	// Current paths
	protected ArrayList<Polyline> paths = new ArrayList<Polyline>();
	protected boolean reloadPaths = true;

	protected GoogleMap googleMap;
	protected View mapView;
	protected MapOverlay mapOverlay;
	protected ImageButton myLocationImageButton;
	protected MyTracksProviderUtils myTracksProviderUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapOverlay = new MapOverlay(this);
	}
	protected String getURLHost() {
		return ((AthleteApplication) getApplication()).getUrlHost();
	}

	protected String getPublicKey() {
		return ((AthleteApplication) getApplication()).getPublicKey();
	}

	protected String getPrivateKey() {
		return ((AthleteApplication) getApplication()).getPrivateKey();
	}

	/**
	 * Returns true if the location is visible.
	 * 
	 * @param location
	 *            the location
	 */
	/*
	 * protected boolean isVisible(Location location) { if (location == null ||
	 * mapView == null) { return false; } GeoPoint mapCenter =
	 * mapView.getMapCenter(); int latitudeSpan = mapView.getLatitudeSpan(); int
	 * longitudeSpan = mapView.getLongitudeSpan();
	 * 
	 * 
	 * The bottom of the mapView is obscured by the zoom controls, subtract its
	 * height from the visible area.
	 * 
	 * GeoPoint zoomControlBottom = mapView.getProjection().fromPixels(0,
	 * mapView.getHeight()); GeoPoint zoomControlTop =
	 * mapView.getProjection().fromPixels( 0, mapView.getHeight() -
	 * mapView.getZoomButtonsController().getZoomControls() .getHeight()); int
	 * zoomControlMargin = Math.abs(zoomControlTop.getLatitudeE6() -
	 * zoomControlBottom.getLatitudeE6()); GeoRect geoRect = new
	 * GeoRect(mapCenter, latitudeSpan, longitudeSpan); geoRect.top +=
	 * zoomControlMargin;
	 * 
	 * GeoPoint geoPoint = LocationUtils.getGeoPoint(location); return
	 * geoRect.contains(geoPoint); }
	 */

	/**
	 * Shows the track.
	 * 
	 * @param track
	 *            the track
	 */
	public void showMarker(long trackId, long id) {
		/*
		 * Synchronize to prevent race condition in changing markerTrackId and
		 * markerId variables.
		 */
		synchronized (this) {
			if (currentTrack != null && currentTrack.getId() == trackId) {
				showMarker(id);
				markerTrackId = -1L;
				markerId = -1L;
				return;
			}
			markerTrackId = trackId;
			markerId = id;
		}
	}

	public void showMarker(final long id) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (googleMap == null) {
					return;
				}
				MyTracksProviderUtils MyTracksProviderUtils = Factory
						.get(BaseActivityMap.this);
				Waypoint waypoint = MyTracksProviderUtils.getWaypoint(id);
				if (waypoint != null && waypoint.getLocation() != null) {
					Location location = waypoint.getLocation();
					LatLng latLng = new LatLng(location.getLatitude(),
							location.getLongitude());
					keepCurrentLocationVisible = false;
					zoomToCurrentLocation = false;
					CameraUpdate cameraUpdate = CameraUpdateFactory
							.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
					googleMap.moveCamera(cameraUpdate);
				}
			}
		});
	}

	protected void showTrack(Track track) {
		if (googleMap == null || track == null || track.getNumberOfPoints() < 2) {
			return;
		}

		TripStatistics tripStatistics = track.getTripStatistics();

		int latitudeSpanE6 = tripStatistics.getTop()
				- tripStatistics.getBottom();
		int longitudeSpanE6 = tripStatistics.getRight()
				- tripStatistics.getLeft();

		if (latitudeSpanE6 > 0 && latitudeSpanE6 < 180E6 && longitudeSpanE6 > 0
				&& longitudeSpanE6 < 360E6) {
			LatLng southWest = new LatLng(tripStatistics.getBottomDegrees(),
					tripStatistics.getLeftDegrees());
			LatLng northEast = new LatLng(tripStatistics.getTopDegrees(),
					tripStatistics.getRightDegrees());
			LatLngBounds bounds = LatLngBounds.builder().include(southWest)
					.include(northEast).build();
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
					bounds, mapView.getWidth(), mapView.getHeight(),
					MAP_VIEW_PADDING);
			googleMap.moveCamera(cameraUpdate);
		}
	}

	protected LatLng getDefaultLatLng() {
		MyTracksProviderUtils myTracksProviderUtils = MyTracksProviderUtils.Factory
				.get(this);
		Location location = myTracksProviderUtils.getLastValidTrackPoint();
		if (location != null) {
			return new LatLng(location.getLatitude(), location.getLongitude());
		} else {
			return new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
		}
	}

	protected boolean isLocationVisible(Location location) {
		if (location == null || googleMap == null) {
			return false;
		}
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		return googleMap.getProjection().getVisibleRegion().latLngBounds
				.contains(latLng);
	}

	@Override
	public void onLocationStateChanged(LocationState locationState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHeadingChanged(double heading) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelectedTrackChanged(Track track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrackUpdated(Track track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearTrackPoints() {
		mapOverlay.clearPoints();
		reloadPaths = true;
	}

	@Override
	public void onSampledInTrackPoint(Location location) {
		mapOverlay.addLocation(location);

	}

	@Override
	public void onSampledOutTrackPoint(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSegmentSplit(Location location) {
		// TODO Auto-generated method stub
		mapOverlay.addSegmentSplit();
	}

	@Override
	public void onNewTrackPointsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMetricUnitsChanged(boolean metricUnits) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onReportSpeedChanged(boolean reportSpeed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onMinRecordingDistanceChanged(int minRecordingDistance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearWaypoints() {
		// TODO Auto-generated method stub
		 mapOverlay.clearWaypoints();
	}

	@Override
	public void onNewWaypoint(Waypoint waypoint) {
		// TODO Auto-generated method stub
		if (waypoint != null
				&& LocationUtils.isValidLocation(waypoint.getLocation())) {
			mapOverlay.addWaypoint(waypoint);
		}
	}

	@Override
	public void onNewWaypointsDone() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (googleMap != null) {
					mapOverlay.update(googleMap, paths, true);
				}
			}
		});

	}
}
