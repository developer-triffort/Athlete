package com.athlete.activity.track.details;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.bl.UserBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.google.android.apps.mytracks.MapOverlay;
import com.athlete.google.android.apps.mytracks.MyMapFragment;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils.LocationIterator;
import com.athlete.google.android.apps.mytracks.content.Track;
import com.athlete.google.android.apps.mytracks.content.TrackDataHub;
import com.athlete.google.android.apps.mytracks.content.TrackDataType;
import com.athlete.google.android.apps.mytracks.services.TrackRecordingService;
import com.athlete.google.android.apps.mytracks.util.ApiAdapterFactory;
import com.athlete.google.android.apps.mytracks.util.LocationUtils;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.StatsUtils;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.model.WorkOut;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.PostRouteFavoriteTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class ActivityRouteDetails extends BaseActivityMap {

	// True to keep my location visible.
	private boolean metricUnits;

	private ImageButton imageBtnStar;
	private TextView mTxtTitle, mTxtTime, mTxtClimbOrCalories,
			mTxtClimbOrCaloriesLabel, mTxtDistanceUnit;
	private TrackDataHub trackDataHub;
	private SeekBar seekBar;
	private int size3dp;
	private boolean zoomToMyLocation;

	// graph

	private FrameLayout graphFrame;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mCurrentSeries;
	private XYSeriesRenderer mCurrentRenderer;
	private String mDateFormat;
	private GraphicalView mChartView;
	private List<Location> locations = new LinkedList<Location>();
	private List<Double> distance = new LinkedList<Double>();
	private List<Long> time = new LinkedList<Long>();
	private List<Double> speed = new LinkedList<Double>();
	private double totalDistance;
	private long totalTime;
	private long trackID;
	private List<Double> elevationArr;
	private double maxElevation;
	private double maxSpeed;
	private double minSpeed;
	private User user;

	private double minElevation = 0;
	private List<Double> avgArr = new LinkedList<Double>();

	private WorkOut workOut;
	private WorkoutBL workoutBL;

	private RelativeLayout progressBar;
	private ImageView splash;
	private Animation animLarge;

	private final double oneMinute = 60.0;
	private final double double100 = 100d;
	private double maxDiff;
	MyMapFragment mfg;
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mapView.clearFocus();
		
	}

	private String getUserName() {
		return getSharedPreferences(com.athlete.Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.USER_NAME, null);
		
	}

	private String getApikey() {
		return getSharedPreferences(com.athlete.Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.API_KEY, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityRouteDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.ROUTE_DETAILS);
	}

	private void initilizeMap() {
		if (googleMap == null) {
			mfg = (MyMapFragment) getFragmentManager().findFragmentById(
					R.id.mapViewRouteDetails);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapOverlay = new MapOverlay(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.actv_route_details_with_seekbar);
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
		myTracksProviderUtils = MyTracksProviderUtils.Factory.get(this);
		progressBar = (RelativeLayout) findViewById(R.id.progressBar);
		splash = (ImageView) findViewById(R.id.imVLoading);
		initilizeMap();
		//mapView = (MapView) findViewById(R.id.mapViewRouteDetails);
		//List<Overlay> overlays = mapView.getOverlays();
		//overlays.clear();

		//overlays.add(mapOverlay);
		//mapView.invalidate();
		mapOverlay.setShowEndMarker(true);
		//mapView.requestFocus();
		seekBar = (SeekBar) findViewById(R.id.seekBarLocation);
		graphFrame = (FrameLayout) findViewById(R.id.graph);
		//ApiAdapterFactory.getApiAdapter().disableHardwareAccelerated(mapView);
		trackID = getIntent().getLongExtra(Constants.INTENT_KEY.ID, -1L);
		int workoutID = getIntent().getIntExtra(
				Constants.INTENT_KEY.WORKOUT_ID, -1);

		workoutBL = new WorkoutBL(getHelper());
		workOut = workoutBL.getBy(String.valueOf(workoutID));

		String userId = getIntent().getStringExtra(
				Constants.INTENT_KEY.USER_DETAILS);
		Track mTrack = myTracksProviderUtils.getTrack(trackID);

		user = new UserBL(getHelper()).getBy(userId);
		PreferencesUtils.setLong(this, R.string.selected_track_id_key,
				mTrack.getId());
		metricUnits = PreferencesUtils.getMetricUnit(ActivityRouteDetails.this);
		trackDataHub = TrackDataHub.newInstance(this);
		trackDataHub.loadTrack(mTrack.getId());
		if (trackDataHub != null) {
			trackDataHub.start();
		}
		trackDataHub.registerTrackDataListener(this, EnumSet.of(
				TrackDataType.SELECTED_TRACK,
				TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE,
				TrackDataType.LOCATION, TrackDataType.HEADING));
		//locationFactory = new DoubleBufferedLocationFactory();
		mTxtClimbOrCalories = (TextView) findViewById(R.id.txtClimbOrCalories);
		mTxtClimbOrCaloriesLabel = (TextView) findViewById(R.id.txtClimbOrCaloriesLabel);
		mTxtTime = (TextView) findViewById(R.id.txtTime);
		mTxtTitle = (TextView) findViewById(R.id.txtTitle);
		mTxtDistanceUnit = (TextView) findViewById(R.id.txtDistanceUnit);
		imageBtnStar = (ImageButton) findViewById(R.id.btnStar);
		if (workOut.isFavorite()) {
			imageBtnStar.setImageResource(R.drawable.star_full);
		}
		imageBtnStar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postFavorite();
			}
		});
		elevationArr = new LinkedList<Double>();
		if (metricUnits) {
			mTxtClimbOrCaloriesLabel.setText(getString(R.string.meter) + " "
					+ getString(R.string.elevation_route_details));
			mTxtDistanceUnit.setText(getString(R.string.kilometer) + " "
					+ getString(R.string.run_lower));
		} else {
			mTxtClimbOrCaloriesLabel.setText(getString(R.string.ft) + " "
					+ getString(R.string.elevation_route_details));
			mTxtDistanceUnit.setText(getString(R.string.mi) + " "
					+ getString(R.string.run_lower));
		}
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});

		size3dp = CommonHelper.getPX(3, ActivityRouteDetails.this);
		showTrack(mTrack);
		getLocationArr();
		prepareMyCharts();
		drawGraph();
		// Start animating the image
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (trackDataHub != null) {
			trackDataHub.stop();
		}

	}

	private void getLocationArr() {
		/*LocationIterator iterator = myTracksProviderUtils
				.getTrackPointLocationIterator(trackID, 1, false,locationFactory);*/
		LocationIterator iterator = myTracksProviderUtils
				.getTrackPointLocationIterator(trackID, 1, false,MyTracksProviderUtils.DEFAULT_LOCATION_FACTORY);
		Location locPrev = null;
		while (iterator.hasNext()) {
			Location location = new Location(iterator.next());
			if (location.getLatitude() == TrackRecordingService.PAUSE_LATITUDE) {
				location = new Location(iterator.next());
				location = new Location(iterator.next());
				locPrev = null;
			}
			locations.add(location);
			if (locPrev != null) {
				double distance = location.distanceTo(locPrev);
				long time = (location.getTime() - locPrev.getTime()) / 1000;
				double speedDouble = distance / time;
				totalDistance += distance;
				totalTime += time;
				speed.add(speedDouble);

				speedDouble *= UnitConversions.MS_TO_KMH;
				if (!metricUnits) {
					speedDouble *= UnitConversions.KM_TO_MI;
				}
				double pace = speedDouble == 0 ? 0.0 : double100 / speedDouble;
				pace = (int) pace + ((int) ((pace - (int) pace) * oneMinute))
						/ double100;
				maxSpeed = Math.max(maxSpeed, pace);
				if (minSpeed == 0.0) {
					minSpeed = pace;
				}
				minSpeed = Math.min(minSpeed, pace);
				avgArr.add(pace);
			} else {
				speed.add(0.0);
				avgArr.add(0.0);
				if (minElevation == 0) {
					minElevation = CommonHelper.getElevationDouble(
							location.getAltitude(), metricUnits);
				}
			}

			elevationArr.add(CommonHelper.getElevationDouble(
					location.getAltitude(), metricUnits));
			maxElevation = Math.max(maxElevation, CommonHelper
					.getElevationDouble(location.getAltitude(), metricUnits));
			minElevation = Math.min(minElevation, CommonHelper
					.getElevationDouble(location.getAltitude(), metricUnits));
			distance.add(totalDistance);
			time.add(totalTime);

			locPrev = location;
		}
		iterator.close();
		if (locations != null && !locations.isEmpty()) {
			setSeekBar();
		}
	}

	private void setSeekBar() {
		seekBar.setMax(locations.size() - 1);
		setTextViewByProgress(0);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				setTextViewByProgress(progress);
			}
		});
	}

	private void setTextViewByProgress(int progress) {
		mTxtClimbOrCalories.setText(CommonHelper.getElevation(
				locations.get(progress).getAltitude(), metricUnits));
		updateCurrentLocation(locations.get(progress));
		// time
		mTxtTime.setText(StringUtils.formatElapsedTime(time.get(progress)
				* Constants.ONE_SECOND));

		// distance
		StatsUtils.setDistanceValue(ActivityRouteDetails.this,
				R.id.txtDistance, 0, distance.get(progress), metricUnits);

		// avg pace
		StatsUtils.setSpeedValue(ActivityRouteDetails.this, R.id.txtAvgPace,
				speed.get(progress), metricUnits, false);
	}

	@Override
	public void onLocationStateChanged(LocationState locationState) {

	}

	@Override
	public void onLocationChanged(Location location) {
		if (isSelectedTrackRecording() && currentLocation == null && location != null) {
			zoomToCurrentLocation = true;
		}
		currentLocation = location;
		updateCurrentLocation(location);
	}

	@Override
	public void onHeadingChanged(double heading) {
		if (mapOverlay.setHeading((float) heading)) {
			//mapView.postInvalidate();

		}

	}

	@Override
	public void onSelectedTrackChanged(final Track track) {
		/*ActivityRouteDetails.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boolean hasTrack = track != null;
				mapOverlay.setTrackDrawingEnabled(hasTrack);

				if (hasTrack) {
					synchronized (this) {
						
						 * Synchronize to prevent race condition in changing
						 * markerTrackId and markerId variables.
						 

						updateMap(track);
					}
					mapOverlay.setShowEndMarker(!isSelectedTrackRecording());
				}
				mapView.invalidate();
			}
		});*/
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
	public void onTrackUpdated(Track track) {

	}

	private synchronized boolean isSelectedTrackRecording() {
		return trackDataHub != null && trackDataHub.isSelectedTrackRecording();
	}

	@Override
	public void onSampledOutTrackPoint(Location location) {

	}

	@Override
	public void onSegmentSplit(Location location) {
		mapOverlay.addSegmentSplit();

	}

	@Override
	public void onNewTrackPointsDone() {
		mapView.postInvalidate();

	}

	private void updateMap(Track track) {

		// Show the track
		showTrack(track);

	}

	@Override
	public boolean onMetricUnitsChanged(boolean metricUnits) {

		return false;
	}

	@Override
	public boolean onReportSpeedChanged(boolean reportSpeed) {

		return false;
	}

	@Override
	public boolean onMinRecordingDistanceChanged(int minRecordingDistance) {

		return false;
	}

	private void updateCurrentLocation(final Location currentLocation) {
		/*if (mapOverlay == null || mapView == null) {
			return;
		}

		mapOverlay.setMyLocation(currentLocation);
		mapView.postInvalidate();

		if (currentLocation != null && keepMyLocationVisible
				&& !isVisible(currentLocation)) {
			GeoPoint geoPoint = LocationUtils.getGeoPoint(currentLocation);
			MapController mapController = mapView.getController();
			mapController.animateTo(geoPoint);
			if (zoomToMyLocation) {
				// Only zoom in the first time we show the location.
				zoomToMyLocation = false;
				if (mapView.getZoomLevel() < mapView.getMaxZoomLevel()) {
					mapController.setZoom(mapView.getMaxZoomLevel());
				}
			}
		}*/
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

	// @Override
	// protected void onRestoreInstanceState(Bundle savedState) {
	// super.onRestoreInstanceState(savedState);
	// mDataset = (XYMultipleSeriesDataset) savedState
	// .getSerializable("dataset");
	// mRenderer = (XYMultipleSeriesRenderer) savedState
	// .getSerializable("renderer");
	// mCurrentSeries = (XYSeries) savedState
	// .getSerializable("current_series");
	// mCurrentRenderer = (XYSeriesRenderer) savedState
	// .getSerializable("current_renderer");
	// mDateFormat = savedState.getString("date_format");
	// }
	//
	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// super.onSaveInstanceState(outState);
	// outState.putSerializable("dataset", mDataset);
	// outState.putSerializable("renderer", mRenderer);
	// outState.putSerializable("current_series", mCurrentSeries);
	// outState.putSerializable("current_renderer", mCurrentRenderer);
	// outState.putString("date_format", mDateFormat);
	// }

	private void drawGraph() {
		float smootnes = 0.5f;
		mRenderer.setShowAxes(false);
		mRenderer.setShowLabels(false);
		mRenderer.setShowLegend(false);
		mRenderer.setAntialiasing(true);
		mRenderer.setMargins(new int[] { 0, 0, 0, 0 });
		if (mChartView == null) {

			mChartView = ChartFactory.getCubeLineChartView(this, mDataset,
					mRenderer, smootnes);

			mRenderer.setClickEnabled(true);
			mRenderer.setSelectableBuffer(locations.size());

			graphFrame.addView(mChartView);

		} else {
			mChartView.repaint();
		}

	}

	private void prepareMyCharts() {
		minElevation = 0;
		minSpeed = 0;
		maxElevation = 0;
		maxSpeed = 0;
		String[] titles = new String[] { "1", "2" };
		int[] colors = new int[] { getResources().getColor(R.color.BLUE_TEXT),
				getResources().getColor(R.color.orange_route_details) };

		avgArr.remove(0);
		elevationArr.remove(0);
		for (int i = 0; i < avgArr.size(); i++) {
			avgArr.set(i, avgArr.get(i));
		}
		avgArr = generalizedPoints(avgArr);
		elevationArr = generalizedPoints(elevationArr);
		setMaxMinData(true);
		setMaxMinData(false);
		mRenderer = buildRenderer(colors);
		int length = mRenderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}

		avgArr = generHeight(avgArr, true);
		elevationArr = generHeight(elevationArr, false);

		List<Coordinate> xyAvg = smoothes(avgArr);
		List<Coordinate> xyElev = smoothes(elevationArr);
		mDataset = buildDataset(titles, xyAvg, xyElev);
		setTitleTxt();

	}

	private void setMaxMinData(boolean isAvg) {

		if (isAvg) {
			for (Double value : avgArr) {
				if (minSpeed == 0) {
					minSpeed = value;
				} else {
					minSpeed = Math.min(minSpeed, value);
				}
				maxSpeed = Math.max(maxSpeed, value);
			}
		} else {
			for (Double value : elevationArr) {
				if (minElevation == 0) {
					minElevation = value;
				} else {
					minElevation = Math.min(minElevation, value);
				}
				maxElevation = Math.max(maxElevation, value);
			}
		}
	}

	private List<Double> generalizedPoints(List<Double> values) {
		List<Double> returnValue = new LinkedList<Double>();
		final int lengOfSegm = 10;
		int wightGraph = getWindowManager().getDefaultDisplay().getWidth()
				- CommonHelper.getPX(44, ActivityRouteDetails.this);
		int numOfSegments = (int) (wightGraph / lengOfSegm);
		int pointPerPixel = (int) (values.size() / numOfSegments);
		float generalizedValue = 0.0f;

		int count = 0;
		for (Double value : values) {
			if (count > pointPerPixel) {
				returnValue.add((double) (generalizedValue / count));
				count = 0;
				generalizedValue = 0.0f;
			}

			generalizedValue += value;
			count += 1;
		}

		// add last object
		returnValue.add((double) (generalizedValue / count));
		return returnValue;
	}

	private List<Double> generHeight(List<Double> values, boolean isAVG) {
		List<Double> returnValue = new LinkedList<Double>();
		if (isAVG) {
			if (maxSpeed - minSpeed < maxElevation - minElevation) {
				for (Double value : values) {
					returnValue.add((maxDiff * value) / maxSpeed);
				}
			} else {
				for (Double value : values) {
					returnValue.add(value - minSpeed);
				}
			}
		} else {
			if (maxSpeed - minSpeed < maxElevation - minElevation) {
				for (Double value : values) {
					returnValue.add(value - minElevation);
				}
			} else {
				for (Double value : values) {
					returnValue.add((maxDiff * value) / maxElevation);
				}
			}
		}

		return returnValue;

	}

	private void postFavorite() {
		progressBar.setVisibility(View.VISIBLE);
		imageBtnStar.setVisibility(View.GONE);
		splash.startAnimation(animLarge);
		OnTskCpltListener sendFavorite = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				imageBtnStar.setVisibility(View.VISIBLE);
				splash.clearAnimation();
				progressBar.setVisibility(View.GONE);
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (!result.isError()) {
						workOut.setFavorite(result.getResult());
						workoutBL.createOrUpdate(workOut);
						if (result.getResult()) {
							imageBtnStar.setImageResource(R.drawable.star_full);

						} else {

							imageBtnStar
									.setImageResource(R.drawable.star_empty);
						}
					}
				} catch (Exception e) {
				}
			}
		};
		PostRouteFavoriteTask postFavorite = new PostRouteFavoriteTask(
				ActivityRouteDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(),
				workOut.getRouteID());
		((AthleteApplication) getApplication()).getTaskManager(
				ActivityRouteDetails.this).executeTask(postFavorite,
				sendFavorite, null, true);
	}

	private void setTitleTxt() {
		if (user != null) {
			mTxtTitle.setText(user.getFirstName() + " " + user.getLastName());
		}
	}

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors) {
		maxDiff = Math.max(maxElevation - minElevation, maxSpeed - minSpeed);

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		for (int i = 0; i < colors.length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setLineWidth(size3dp);
			renderer.addSeriesRenderer(r);
		}
		renderer.setYAxisMin(-(maxDiff / 10));
		renderer.setYAxisMax(maxDiff + (maxDiff / 10));
		renderer.setAntialiasing(true);
		return renderer;
	}

	protected XYMultipleSeriesDataset buildDataset(String[] titles,
			List<Coordinate> xyAVG, List<Coordinate> xyElev) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		XYSeries seriesEle = new XYSeries("1", 0);
		for (Coordinate coordinate : xyElev) {

			seriesEle.add(coordinate.getX(), coordinate.getY());
		}
		dataset.addSeries(seriesEle);
		XYSeries seriesAvg = new XYSeries("2", 0);
		for (Coordinate coordinate : xyAVG) {

			seriesAvg.add(coordinate.getX(), coordinate.getY());
		}
		dataset.addSeries(seriesAvg);

		return dataset;
	}

	private List<Coordinate> smoothes(List<Double> value) {

		List<Coordinate> xy = new LinkedList<Coordinate>();
		List<Coordinate> returnXY = new LinkedList<Coordinate>();
		int count = value.size();
		for (Double y : value) {
			xy.add(new Coordinate(xy.size(), y));

		}

		final int segments = 4;
		if (value.size() < 4) {
			return xy;
		}

		float[][] b = new float[segments][4];

		float t = 0.0f;
		float dt = 1.0f / (float) segments;
		for (int i = 0; i < segments; i++, t += dt) {
			float tt = t * t;
			float ttt = tt * t;
			b[i][0] = 0.5f * (-ttt + 2.0f * tt - t);
			b[i][1] = 0.5f * (3.0f * ttt - 5.0f * tt + 2.0f);
			b[i][2] = 0.5f * (-3.0f * ttt + 4.0f * tt + t);
			b[i][3] = 0.5f * (ttt - tt);
		}
		int i = 0;
		returnXY.add(xy.get(0));
		for (int j = 1; j < segments; j++) {
			Coordinate pointI = xy.get(i);
			Coordinate pointIp1 = xy.get(i + 1);
			Coordinate pointIp2 = xy.get(i + 2);
			double px = (b[j][0] + b[j][1]) * pointI.x + b[j][2] * pointIp1.x
					+ b[j][3] * pointIp2.x;
			double py = (b[j][0] + b[j][1]) * pointI.y + b[j][2] * pointIp1.y
					+ b[j][3] * pointIp2.y;

			returnXY.add(new Coordinate(px, py));
		}

		for (int k = 1; k < count - 2; k++) {
			// the first interpolated point is always the original control point
			returnXY.add(xy.get(k));
			for (int j = 1; j < segments; j++) {
				Coordinate pointIm1 = xy.get(k - 1);
				Coordinate pointI = xy.get(k);
				Coordinate pointIp1 = xy.get(k + 1);
				Coordinate pointIp2 = xy.get(k + 2);
				double px = b[j][0] * pointIm1.x + b[j][1] * pointI.x + b[j][2]
						* pointIp1.x + b[j][3] * pointIp2.x;
				double py = b[j][0] * pointIm1.y + b[j][1] * pointI.y + b[j][2]
						* pointIp1.y + b[j][3] * pointIp2.y;
				returnXY.add(new Coordinate(px, py));
			}
		}
		i = count - 2; // second to last control point
		returnXY.add(xy.get(i));
		for (int j = 1; j < segments; j++) {
			Coordinate pointIm1 = xy.get(i - 1);
			Coordinate pointI = xy.get(i);
			Coordinate pointIp1 = xy.get(i + 1);
			double px = b[j][0] * pointIm1.x + b[j][1] * pointI.x
					+ (b[j][2] + b[j][3]) * pointIp1.x;
			double py = b[j][0] * pointIm1.y + b[j][1] * pointI.y
					+ (b[j][2] + b[j][3]) * pointIp1.y;
			returnXY.add(new Coordinate(px, py));
		}

		return xy;

	}

	class Coordinate {
		Coordinate(double x, double y) {
			this.x = x;
			this.y = y;
		}

		Coordinate() {
		}

		private double x;
		private double y;

		public double getX() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public double getY() {
			return y;
		}

		public void setY(double y) {
			this.y = y;
		}
	}
}
