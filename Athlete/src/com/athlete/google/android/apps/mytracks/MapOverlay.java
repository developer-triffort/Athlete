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

package com.athlete.google.android.apps.mytracks;

import static com.athlete.Constants.TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.google.android.apps.mytracks.content.Waypoint;
import com.athlete.google.android.apps.mytracks.maps.TrackPath;
import com.athlete.google.android.apps.mytracks.maps.TrackPathFactory;

import com.athlete.google.android.apps.mytracks.util.LocationUtils;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;


/**
 * A map overlay that displays my location arrow, error circle, and track info.
 * 
 * @author Leif Hendrik Wilden khalid afzal
 */
public class MapOverlay implements 	OnSharedPreferenceChangeListener {

	public static final float WAYPOINT_X_ANCHOR = 13f / 48f;

	private static final float WAYPOINT_Y_ANCHOR = 43f / 48f;
	private static final float MARKER_X_ANCHOR = 50f / 96f;
	private static final float MARKER_Y_ANCHOR = 90f / 96f;
	private static final int INITIAL_LOCATIONS_SIZE = 2048;

	private final Context context;
	private final List<CachedLocation> locations;
	private final BlockingQueue<CachedLocation> pendingLocations;
	private final List<Waypoint> waypoints;
	/*
	 * private final List<CachedLocation> points; private final List<Location>
	 * waypointsLocation; private final List<Location> locations; private final
	 * BlockingQueue<CachedLocation> pendingPoints;
	 */
	/*
	 * private final Drawable statsMarker; private final Drawable
	 * waypointMarker; private final Drawable startMarker; private final
	 * Drawable endMarker; private final int markerWidth; private final int
	 * markerHeight; private final Paint errorCirclePaint;
	 */
	private TrackPath trackPath;

	private boolean trackDrawingEnabled;
	private boolean showEndMarker = true;
	private int headingIndex = 0;
	private Location myLocation;
	private Paint paint;
	private GeoPoint lastReferencePoint;
	private Rect lastViewRect;
	private MapView mapView;
	private double ONE_KM = 1;

	/**
	 * A pre-processed {@link Location} to speed up drawing.
	 * 
	 * @author Jimmy Shih
	 */
	public static class CachedLocation {

		private final boolean valid;
		private final LatLng latLng;
		private final int speed;

		/**
		 * Constructor for an invalid cached location.
		 */
		public CachedLocation() {
			this.valid = false;
			this.latLng = null;
			this.speed = -1;
		}

		/**
		 * Constructor for a potentially valid cached location.
		 */
		public CachedLocation(Location location) {

			this.valid = LocationUtils.isValidLocation(location);
			this.latLng = valid ? new LatLng(location.getLatitude(),
					location.getLongitude()) : null;
			this.speed = (int) Math.floor(location.getSpeed()
					* UnitConversions.MS_TO_KMH);
		}

		/**
		 * Returns true if the location is valid.
		 */
		public boolean isValid() {
			return valid;
		}

		/**
		 * Gets the {@link GeoPoint}.
		 */
		public LatLng getLatLng() {
			return latLng;
		}

		/**
		 * Gets the speed in kilometers per hour.
		 */
		public int getSpeed() {
			return speed;
		}
	};

	private Drawable mMarker;
	private int level;
	private Timer timer;
	private boolean isRouteDetails = false;

//	public MapOverlay(Context ctx, boolean isRouteDetails) {
//		this(ctx);
//		this.isRouteDetails = isRouteDetails;
//		if (PreferencesUtils.getMetricUnit(ctx)) {
//			ONE_KM *= Constants.ONE_THSND;
//		} else {
//			ONE_KM *= UnitConversions.MI_TO_M;
//		}
//	}

	public MapOverlay(Context context) {
		this.context = context;

		this.waypoints = new ArrayList<Waypoint>();
		this.locations = new ArrayList<CachedLocation>(INITIAL_LOCATIONS_SIZE);
		this.pendingLocations = new ArrayBlockingQueue<CachedLocation>(Constants.MAX_DISPLAYED_TRACK_POINTS, true);
		context.getSharedPreferences(Constants.SETTINGS_NAME,Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				this);
		onSharedPreferenceChanged(null, null);
	}

	/**
	 * Add a location to the map overlay.
	 * <p>
	 * NOTE: This method doesn't take ownership of the given location, so it is
	 * safe to reuse the same location while calling this method.
	 * 
	 * @param location
	 *            the location
	 */
	public void addLocation(Location location) {
		if (!pendingLocations.offer(new CachedLocation(location))) {
			Log.e(TAG, "Unable to add to pendingPoints.");
		}
	}

	/*
	 * public void setShowWaypoint(boolean isWaypoint, Location
	 * waypointLocation) { this.waypointsLocation.add(waypointLocation); };
	 */
	/**
	 * Adds a segment split to the map overlay.
	 */
	public void addSegmentSplit() {
		// Queue up in the pendingPoints until it's merged with points.
		if (!pendingLocations.offer(new CachedLocation())) {
			Log.e(TAG, "Unable to add to pendingPoints");
		}
	}

	/**
	 * Clears the locations.
	 */
	public void clearPoints() {
		synchronized (locations) {
			locations.clear();
			pendingLocations.clear();
		}
	}

	public void addWaypoint(Waypoint waypoint) {
		synchronized (waypoints) {
			waypoints.add(waypoint);
		}
	}

	public void clearWaypoints() {
		synchronized (waypoints) {
			waypoints.clear();
		}
	}

	/**
	 * Sets whether to draw the track or not.
	 * 
	 * @param trackDrawingEnabled
	 *            true to draw track
	 */
	/*
	 * public void setTrackDrawingEnabled(boolean trackDrawingEnabled) {
	 * this.trackDrawingEnabled = trackDrawingEnabled; }
	 */

	/**
	 * Sets whether to draw the end maker or not.
	 * 
	 * @param showEndMarker
	 *            true to draw end marker
	 */
	public void setShowEndMarker(boolean showEndMarker) {
		this.showEndMarker = showEndMarker;
	}

	/**
	 * Sets my location.
	 * 
	 * @param myLocation
	 *            my location
	 */
	/*
	 * public void setMyLocation(Location myLocation) { this.myLocation =
	 * myLocation; }
	 */

	public void update(GoogleMap googleMap, ArrayList<Polyline> paths,
			boolean reload) {
		synchronized (locations) {
			// Merge pendingLocations with locations
			int newLocations = pendingLocations.drainTo(locations);
			boolean needReload = reload || trackPath.updateState();
			if (needReload) {
				googleMap.clear();
				paths.clear();
				trackPath.updatePath(googleMap, paths, 0, locations);
				updateStartAndEndMarkers(googleMap);
				updateWaypoints(googleMap);
			} else {
				if (newLocations != 0) {
					int numLocations = locations.size();
					trackPath.updatePath(googleMap, paths, numLocations
							- newLocations, locations);
				}
			}
		}
	}

	private void updateStartAndEndMarkers(GoogleMap googleMap) {
		// Add the end marker  no need to show marker
		/*if (showEndMarker) {
			for (int i = locations.size() - 1; i >= 0; i--) {
				CachedLocation cachedLocation = locations.get(i);
				if (cachedLocation.valid) {
					MarkerOptions markerOptions = new MarkerOptions()
							.position(cachedLocation.getLatLng())
							.anchor(MARKER_X_ANCHOR, MARKER_Y_ANCHOR)
							.draggable(false)
							.visible(true)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_dot));
					googleMap.addMarker(markerOptions);
					break;
				}
			}
		}

		// Add the start marker
		for (int i = 0; i < locations.size(); i++) {
			CachedLocation cachedLocation = locations.get(i);
			if (cachedLocation.valid) {
				MarkerOptions markerOptions = new MarkerOptions()
						.position(cachedLocation.getLatLng())
						.anchor(MARKER_X_ANCHOR, MARKER_Y_ANCHOR)
						.draggable(false)
						.visible(true)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.green_dot));
				googleMap.addMarker(markerOptions);
				break;
			}
		}*/
	}

	/**
	 * Updates the waypoints.
	 * 
	 * @param googleMap
	 *            the google map.
	 */
	private void updateWaypoints(GoogleMap googleMap) {
		/*synchronized (waypoints) {
			for (Waypoint waypoint : waypoints) {
				Location location = waypoint.getLocation();
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				int drawableId = waypoint.getType() == Waypoint.TYPE_STATISTICS ? R.drawable.yellow_pushpin
						: R.drawable.blue_pushpin;
				MarkerOptions markerOptions = new MarkerOptions()
						.position(latLng)
						.anchor(WAYPOINT_X_ANCHOR, WAYPOINT_Y_ANCHOR)
						.draggable(false).visible(true)
						.icon(BitmapDescriptorFactory.fromResource(drawableId))
						.title(String.valueOf(waypoint.getId()));
				googleMap.addMarker(markerOptions);
			}
		}*/
	}

	/**
	 * Sets the heading.
	 * 
	 * @param heading
	 *            the heading
	 * @return true if the visible heading has changed.
	 */
	public boolean setHeading(float heading) {
		/*
		 * Use -heading because the arrow images are counter-clockwise rather
		 * than clockwise.
		 */
		int index = Math.round(-heading / 360 * 18);
		while (index < 0) {
			index += 18;
		}
		while (index > 17) {
			index -= 18;
		}
		if (index != headingIndex) {
			headingIndex = index;
			return true;
		} else {
			return false;
		}
	}

	/*@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		this.mapView = mapView;
		// It's safe to keep projection within a single draw operation
		Projection projection = getMapProjection(mapView);

		if (projection == null) {
			Log.w(TAG, "No projection, unable to draw.");
			return;
		}

		if (trackDrawingEnabled) {
			// Get the current viewing Rect
			Rect viewRect = getMapViewRect(mapView);

			// Draw the selected track
			drawTrack(canvas, projection, viewRect);

			// Draw the start and end markers
			drawMarkers(canvas, projection);

			// Draw the waypoints
			if (!isRouteDetails) {
				drawWaypoints(canvas, projection);
			}
		}

		// Draw the current location
		drawMyLocation(canvas, projection);
	}
*/
	/*@Override
	public boolean onTap(GeoPoint geoPoint, MapView mapView) {
		if (geoPoint.equals(mapView.getMapCenter())) {
			
			 * There is (unfortunately) no good way to determine whether the tap
			 * was caused by an actual tap on the screen or the track ball. If
			 * the location is equal to the map center,then it was a track ball
			 * press with very high likelihood.
			 
			return false;
		}

		return super.onTap(geoPoint, mapView);
	}*/

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (PreferencesUtils.getKey(context, R.string.track_color_mode_key).equals(key)) {
			trackPath = TrackPathFactory.getTrackPathPainter(context);
		}
	}

	/**
	 * Gets the points.
	 */
	/*@VisibleForTesting
	public List<CachedLocation> getPoints() {
		return points;
	}*/

	/**
	 * Gets the track path painter.
	 */
	/*@VisibleForTesting
	public TrackPathPainter getTrackPathPainter() {
		return trackPathPainter;
	}

	*//**
	 * Sets the track path painter.
	 * 
	 * @param trackPathPainter
	 *            the track path painter
	 *//*
	@VisibleForTesting
	public void setTrackPathPainter(TrackPathPainter trackPathPainter) {
		this.trackPathPainter = trackPathPainter;
	}

	*//**
	 * Gets the map view projection.
	 * 
	 * @param mapView
	 *            the map view
	 *//*
	@VisibleForTesting
	protected Projection getMapProjection(MapView mapView) {
		return mapView.getProjection();
	}*/

	/**
	 * Gets the map view Rect.
	 * 
	 * @param mapView
	 *            the map view
	 */
	/*@VisibleForTesting
	protected Rect getMapViewRect(MapView mapView) {
		int width = mapView.getLongitudeSpan();
		int height = mapView.getLatitudeSpan();
		int centerX = mapView.getMapCenter().getLongitudeE6();
		int centerY = mapView.getMapCenter().getLatitudeE6();
		return new Rect(centerX - width / 2, centerY - height / 2, centerX
				+ width / 2, centerY + height / 2);
	}

	*//**
	 * Gets number of locations.
	 *//*
	@VisibleForTesting
	int getNumLocations() {
		synchronized (points) {
			return points.size() + pendingPoints.size();
		}
	}*/

	/**
	 * Draws the track.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param projection
	 *            the projection
	 * @param viewRect
	 *            the view rect
	 */
	/*private void drawTrack(Canvas canvas, Projection projection, Rect viewRect) {
		boolean draw;

		synchronized (points) {
			// Merge the pending points with the list of cached locations.
			GeoPoint referencePoint = projection.fromPixels(0, 0);
			int newPoints = pendingPoints.drainTo(points);
			boolean newProjection = !viewRect.equals(lastViewRect)
					|| !referencePoint.equals(lastReferencePoint);
			// Call updateState first to trigger its side effects.
			boolean currentPathValid = !trackPathPainter.updateState()
					&& !newProjection && trackPathPainter.hasPath();
			if (newPoints == 0 && currentPathValid) {
				// No need to update
				draw = true;
			} else {
				int numPoints = points.size();
				if (numPoints < 2) {
					// Not enough points to draw a path
					draw = false;
				} else if (currentPathValid) {
					// Incremental update of the path
					draw = true;
					trackPathPainter.updatePath(projection, viewRect, numPoints
							- newPoints, points);
				} else {
					// Reload the path
					draw = true;
					trackPathPainter.clearPath();

					trackPathPainter
							.updatePath(projection, viewRect, 0, points);
				}
			}
			lastReferencePoint = referencePoint;
			lastViewRect = viewRect;
		}
		if (draw) {
			trackPathPainter.drawPath(canvas);
		}
	}
*/
	/**
	 * Draws the start and end markers.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param projection
	 *            the projection
	 */
	/*private void drawMarkers(Canvas canvas, Projection projection) {

		// Draw the end marker
		if (showEndMarker) {
			if (isRouteDetails) {
				drawWaypointsLocation(canvas, projection);
			}
			for (int i = locations.size() - 1; i >= 0; i--) {
				if (locations.get(i).valid) {
					drawElement(
							canvas,
							projection,
							locations.get(i).latLng,
							endMarker,
							-endMarker.getIntrinsicWidth(),
							-(int) (endMarker.getIntrinsicHeight() * Constants.MARKER_Y_OFFSET_PERCENTAGE));
					break;
				}
			}
		}

		// Draw the start marker
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).valid) {
				drawElement(
						canvas,
						projection,
						points.get(i).geoPoint,
						startMarker,
						0,
						-(int) (startMarker.getIntrinsicHeight() * Constants.MARKER_Y_OFFSET_PERCENTAGE));
				break;
			}
		}
	}*/

	/**
	 * Draws the waypoints.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param projection
	 *            the projection
	 */

	/*private void drawWaypointsLocation(Canvas canvas, Projection projection) {
		int countWaypoints = 0;
		double waypointValue = ONE_KM;
		synchronized (points) {

			double countDistanceTo = 0;
			for (int i = 0; i < locations.size(); i++) {

				if (i != 0 && locations.get(0) != null
						&& locations.get(i - 1) != null
						&& locations.get(i) != null) {

					countDistanceTo = countDistanceTo
							+ locations.get(i - 1).distanceTo(locations.get(i));

					if (countDistanceTo >= waypointValue) {
						countWaypoints++;
						Location waypointLocation = new Location(
								locations.get(i));
						double prevDistance = countDistanceTo
								- locations.get(i - 1).distanceTo(
										locations.get(i));
						double prevLongitude = locations.get(i - 1)
								.getLongitude();
						double prevLatitude = locations.get(i - 1)
								.getLatitude();

						double lastDistance = countDistanceTo;
						double lastLongitude = locations.get(i).getLongitude();
						double lastLatitude = locations.get(i).getLatitude();

						double k = ((lastDistance - prevDistance) / (waypointValue - prevDistance));
						double longitude = (lastLongitude - prevLongitude) / k;
						waypointLocation
								.setLongitude(prevLongitude + longitude);

						double latitude = (lastLatitude - prevLatitude) / k;
						waypointValue += ONE_KM;
						waypointLocation.setLatitude(prevLatitude + latitude);

						drawElement(canvas, projection,
								LocationUtils.getGeoPoint(waypointLocation),
								waypointMarker,
								-waypointMarker.getIntrinsicWidth() / 2,
								-waypointMarker.getIntrinsicHeight() / 2);

						Point point = new Point();
						projection.toPixels(
								LocationUtils.getGeoPoint(waypointLocation),
								point);
						Rect bounds = new Rect();
						paint.getTextBounds(String.valueOf(countWaypoints), 0,
								String.valueOf(countWaypoints).length(), bounds);

						canvas.drawText(String.valueOf(countWaypoints),
								point.x, (point.y + bounds.height() / 2), paint);

					}
				}
			}
		}

	}*/

	/*private void drawWaypoints(Canvas canvas, Projection projection) {
		synchronized (points) {

			for (int i = 0; i < waypointsLocation.size(); i++) {

				drawElement(canvas, projection,
						LocationUtils.getGeoPoint(waypointsLocation.get(i)),
						waypointMarker,
						-waypointMarker.getIntrinsicWidth() / 2,
						-waypointMarker.getIntrinsicHeight() / 2);

				Point point = new Point();
				projection.toPixels(
						LocationUtils.getGeoPoint(waypointsLocation.get(i)),
						point);
				Rect bounds = new Rect();
				paint.getTextBounds(String.valueOf(i + 1), 0,
						String.valueOf(i + 1).length(), bounds);

				canvas.drawText(String.valueOf(i + 1), point.x,
						(point.y + bounds.height() / 2), paint);

			}

		}

	}
*/
	/**
	 * Draws my location.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param projection
	 *            the projection
	 */
	/*private void drawMyLocation(Canvas canvas, Projection projection) {
		if (myLocation == null) {
			return;
		}
		Point point = new Point();
		projection.toPixels(LocationUtils.getGeoPoint(myLocation), point); // 3
		int width = mMarker.getIntrinsicWidth();
		int height = mMarker.getIntrinsicHeight();
		if (isRouteDetails) {
			mMarker.setLevel(7);
		} else {
			mMarker.setLevel(level);
		}

		mMarker.setBounds(point.x - width / 2, point.y - height / 2, point.x
				+ width / 2, point.y + height / 2);
		mMarker.draw(canvas);

	}*/

	/**
	 * Draws an element.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param projection
	 *            the projection
	 * @param geoPoint
	 *            the geo point
	 * @param drawable
	 *            the drawable
	 * @param offsetX
	 *            the x offset
	 * @param offsetY
	 *            the y offset
	 * @return the point of the drawing.
	 */
	private Point drawElement(Canvas canvas, Projection projection,
			GeoPoint geoPoint, Drawable drawable, int offsetX, int offsetY) {

		Point point = new Point();

		projection.toPixels(geoPoint, point);
		canvas.save();
		canvas.translate(point.x + offsetX, point.y + offsetY);

		drawable.draw(canvas);

		canvas.restore();

		return point;
	}
}
