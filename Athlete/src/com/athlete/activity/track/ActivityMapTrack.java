/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.athlete.activity.track;

import java.util.EnumSet;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.athlete.R;
import com.athlete.activity.track.details.BaseActivityMap;
import com.athlete.google.android.apps.mytracks.MapOverlay;
import com.athlete.google.android.apps.mytracks.MyMapFragment;
import com.athlete.google.android.apps.mytracks.content.Track;

import com.athlete.google.android.apps.mytracks.content.TrackDataType;

import com.athlete.google.android.apps.mytracks.util.GoogleLocationUtils;

import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;

import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.LocationSource;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * A fragment to display map to the user.
 * 
 * @author Leif Hendrik Wilden
 * @author Rodrigo Damazio
 */
public class ActivityMapTrack extends BaseActivityMap {
	MyMapFragment mfg;
	/*
	 * private static final String KEY_CURRENT_LOCATION = "currentLocation";
	 * private static final String KEY_KEEP_MY_LOCATION_VISIBLE =
	 * "keepMyLocationVisible";
	 */

	// True to zoom to my location. Only apply when keepMyLocationVisible is
	private boolean zoomToMyLocation;
	// The current location. Set in onCurrentLocationChanged.
	// UI elements
	private double waypointValue;
	private double ONE_KM_OR_MI;
	private boolean isMetric;
	private TabActivityTrack tabactivity;

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

	private void initilizeMap() {
		if (googleMap == null) {
			mfg = (MyMapFragment) getFragmentManager().findFragmentById(
					R.id.map_view);
			// mfg.setHasOptionsMenu(true);
			mapView = mfg.getMapView();
			googleMap = mfg.getMap();
			googleMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.setIndoorEnabled(true);
			googleMap.setLocationSource(new LocationSource() {

				@Override
				public void deactivate() {
					onLocationChangedListener = null;
				}

				@Override
				public void activate(OnLocationChangedListener listener) {
					onLocationChangedListener = listener;
				}
			});

			googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition cameraPosition) {
					if (keepCurrentLocationVisible && currentLocation != null
							&& !isLocationVisible(currentLocation)) {
						keepCurrentLocationVisible = false;
						zoomToCurrentLocation = false;
					}
				}
			});
			// check if map is created successfully or not
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					getDefaultLatLng(), googleMap.getMinZoomLevel()));
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.map);

		initilizeMap();
		myLocationImageButton = (ImageButton) findViewById(R.id.map_my_location);
		myLocationImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				forceUpdateLocation();
				keepCurrentLocationVisible = true;
				zoomToCurrentLocation = true;
				updateCurrentLocation();
			}
		});
		if (bundle != null) {
			keepCurrentLocationVisible = bundle.getBoolean(
					KEEP_CURRENT_LOCATION_VISIBLE_KEY, false);
			zoomToCurrentLocation = bundle.getBoolean(
					ZOOM_TO_CURRENT_LOCATION_KEY, false);
			currentLocation = (Location) bundle
					.getParcelable(CURRENT_LOCATION_KEY);
			updateCurrentLocation();
		}
		// ApiAdapterFactory.getApiAdapter().invalidMenu(this);
		//mapOverlay = new MapOverlay(this);

		isMetric = PreferencesUtils.getMetricUnit(this);
		if (isMetric) {
			ONE_KM_OR_MI = 1000;
		} else {
			ONE_KM_OR_MI = 1;
		}
		waypointValue = ONE_KM_OR_MI;
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.contains("gps")) {
			Toast.makeText(ActivityMapTrack.this, "Gps Disabled",
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
		tabactivity = ((TabActivityTrack) getParent());
		tabactivity.setMapActivity(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		resumeTrackDataHub();
		initilizeMap();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEEP_CURRENT_LOCATION_VISIBLE_KEY,
				keepCurrentLocationVisible);
		outState.putBoolean(ZOOM_TO_CURRENT_LOCATION_KEY, zoomToCurrentLocation);
		if (currentLocation != null) {
			outState.putParcelable(CURRENT_LOCATION_KEY, currentLocation);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		pauseTrackDataHub();
	}

	private synchronized void pauseTrackDataHub() {
		if (trackDataHub != null) {
			trackDataHub.unregisterTrackDataListener(this);
		}
		trackDataHub = null;
	}

	/**
	 * Shows my location.
	 */
	private void showMyLocation() {
		updateTrackDataHub();
		keepCurrentLocationVisible = true;
		zoomToMyLocation = true;
		if (currentLocation != null) {
			updateCurrentLocation();
		}
	}

	/**
	 * Shows the marker.
	 * 
	 * @param trackId
	 *            the track id
	 * @param id
	 *            the marker id
	 */
	@Override
	public void onLocationStateChanged(final LocationState state) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (googleMap == null) {
					return;
				}
				boolean myLocationEnabled = true;
				if (state == LocationState.DISABLED) {
					currentLocation = null;
					myLocationEnabled = false;
				}
				googleMap.setMyLocationEnabled(myLocationEnabled);

				String message;
				boolean isGpsDisabled;
				if (!isSelectedTrackRecording()) {
					message = null;
					isGpsDisabled = false;
				} else {
					switch (state) {
					case DISABLED:
						String setting = getString(GoogleLocationUtils
								.isAvailable(ActivityMapTrack.this) ? R.string.gps_google_location_settings
								: R.string.gps_location_access);
						message = getString(R.string.gps_disabled, setting);
						isGpsDisabled = true;
						break;
					case NO_FIX:
						message = getString(R.string.gps_wait_for_signal);
						isGpsDisabled = false;
						break;
					case BAD_FIX:
						message = getString(R.string.gps_wait_for_better_signal);
						isGpsDisabled = false;
						break;
					case GOOD_FIX:
						message = null;
						isGpsDisabled = false;
						break;
					default:
						throw new IllegalArgumentException("Unexpected state: "
								+ state);
					}
				}
				if (isGpsDisabled) {
					Toast.makeText(ActivityMapTrack.this,R.string.gps_not_found, Toast.LENGTH_LONG).show();

				}
			}
		});
	}

	@Override
	public void onLocationChanged(Location location) {
		if (isSelectedTrackRecording() && currentLocation == null && location != null) {
			zoomToCurrentLocation = true;
		}
		currentLocation = location;
		updateCurrentLocation();
	}
	@Override
	public void onSelectedTrackChanged(final Track track) {
		currentTrack = track;
		boolean hasTrack = track != null;
		if (hasTrack) {
			mapOverlay.setShowEndMarker(!isSelectedTrackRecording());
			synchronized (this) {
				if (track.getId() == markerTrackId) {
					showMarker(markerId);
					markerTrackId = -1L;
					markerId = -1L;
				} else {
					// Show the track
					showTrack(currentTrack);
				}
			}
		}
	}


	@Override
	public void onNewTrackPointsDone() {

		// mapView.postInvalidate();
		runOnUiThread(new Runnable() {
			public void run() {
				if (googleMap != null) {
					mapOverlay.update(googleMap, paths, reloadPaths);
					reloadPaths = false;
				}
			}
		});

	}


	@Override
	public boolean onReportSpeedChanged(boolean reportSpeed) {
		// We don't care.
		return false;
	}


	/**
	 * Resumes the trackDataHub. Needs to be synchronized because trackDataHub
	 * can be accessed by multiple threads.
	 */
	private synchronized void resumeTrackDataHub() {

		trackDataHub = ((TabActivityTrack) getParent()).getTrackDataHub();

		trackDataHub.registerTrackDataListener(ActivityMapTrack.this, EnumSet
				.of(TrackDataType.SELECTED_TRACK,

				TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE,
						TrackDataType.LOCATION, TrackDataType.HEADING));

	}

	/**
	 * Updates the trackDataHub. Needs to be synchronized because trackDataHub
	 * can be accessed by multiple threads.
	 */
	private synchronized void updateTrackDataHub() {
		if (trackDataHub != null) {
			trackDataHub.forceUpdateLocation();
		}
	}

	/**
	 * Returns true if the selected track is recording. Needs to be synchronized
	 * because trackDataHub can be accessed by multiple threads.
	 */
	private synchronized boolean isSelectedTrackRecording() {
		return trackDataHub != null && trackDataHub.isSelectedTrackRecording();
	}

	private synchronized void forceUpdateLocation() {
		if (trackDataHub != null) {
			trackDataHub.forceUpdateLocation();
		}
	}

	/**
	 * Updates the map by either zooming to the requested marker or showing the
	 * track.
	 * 
	 * @param track
	 *            the track
	 */
	private void updateMap(Track track) {
		showTrack(track);
	}

	/**
	 * Updates the current location and centers it if necessary.
	 */
	private void updateCurrentLocation() {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (googleMap == null || onLocationChangedListener == null
						|| currentLocation == null) {
					return;
				}
				onLocationChangedListener.onLocationChanged(currentLocation);
				if (zoomToCurrentLocation
						|| (keepCurrentLocationVisible && !isLocationVisible(currentLocation))) {
					LatLng latLng = new LatLng(currentLocation.getLatitude(),
							currentLocation.getLongitude());
					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							latLng, DEFAULT_ZOOM_LEVEL));
					zoomToCurrentLocation = false;
				}
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
