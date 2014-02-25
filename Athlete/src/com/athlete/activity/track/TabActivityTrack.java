package com.athlete.activity.track;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.TabActivityMain;
import com.athlete.activity.track.music.ActivityMusic;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.UserBL;
import com.athlete.control.PlaySound;
import com.athlete.google.android.apps.mytracks.TrackController;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.content.Track;
import com.athlete.google.android.apps.mytracks.content.TrackDataHub;
import com.athlete.google.android.apps.mytracks.content.TrackDataListener;
import com.athlete.google.android.apps.mytracks.content.TrackDataType;
import com.athlete.google.android.apps.mytracks.content.Waypoint;
import com.athlete.google.android.apps.mytracks.services.ITrackRecordingService;
import com.athlete.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.athlete.google.android.apps.mytracks.stats.TripStatistics;
import com.athlete.google.android.apps.mytracks.util.GoogleLocationUtils;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.StatsUtils;
import com.athlete.google.android.apps.mytracks.util.TrackRecordingServiceConnectionUtils;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;
import com.athlete.model.ProfileUser;
import com.athlete.model.Split;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

@SuppressLint({ "UseSparseArrays", "NewApi" })
@SuppressWarnings("deprecation")
public class TabActivityTrack extends BaseTabTrack implements TrackDataListener {
	
	public static final String EXTRA_TRACK_ID = "track_id";
	public static final String EXTRA_MARKER_ID = "marker_id";
	//------------------------------------------------------
	private final int timeOutForPause = 12;
	private final int timeOutForGPSFOUND = 60;
	private final int accurate = 60;
	private final int countOfSectors = 6;
	private final int oneSector = accurate / countOfSectors;
	private final double oneMinute = 60.0;
	private final double double100 = 100d;
	private final int countDown5 = 5;
	private final int countDown10 = 10;
	private final int countDown30 = 30;
	private final int countSecondRaw = 100;
	private final int countSecondRawMore100 = 900;
	private final int startForRaws = 200;
	private ArrayList<String> pathes = new ArrayList<String>();
	private TextView txtNotifyCamera;
	private boolean isNoFix;
	public static Boolean isTrackStartedFinish;
	private String message;
	private TextView messageTextView;
	private int lostTime;
	private Timer mTimerLostSignal;
	private final double weightDeff = 140;

	private void turnGPSOnOff() {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.contains("gps")) {
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}

	}

	@Override
	public void onBackPressed() {
		if (isStop && time == 0) {
			if (trackDataHub != null) {
				trackDataHub.stop();
				trackRecordingServiceConnection.unbind();
			}
			super.onBackPressed();
			if (mTimerAutoPause != null) {
				mTimerAutoPause.cancel();
			}
			if (mTimerLostSignal != null) {
				mTimerLostSignal.cancel();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.tab_track);
		AnalyticsUtils.sendPageViews(TabActivityTrack.this, "TrackScreen");
		turnGPSOnOff();
		messageTextView = (TextView) findViewById(R.id.map_message);
		((AthleteApplication) getApplication())
				.createNewMediaPlayerVoiceObject();
		((AthleteApplication) getApplication())
				.createNewMediaPlayerMusicObject();
		isTrackStartedFinish = new Boolean(false);
		isNotification = getIntent().getBooleanExtra(
				com.athlete.Constants.INTENT_KEY.NOTIFICATION, false);
		if (isNotification) {
			isStop = false;
		} else {
			PreferencesUtils.setLong(this, R.string.selected_track_id_key,
					PreferencesUtils.RECORDING_TRACK_ID_DEFAULT);
		}
		userBL = new UserBL(getHelper());
		baseOperationsBL = new BaseOperationsBL(getHelper());
		setView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
	}

	private void setView() {
		mTimerAutoPause = new Timer();
		mTimerCountDown = new Timer();
		mTimerLostSignal = new Timer();
		mSplit = new Split();
		mSplits = new LinkedList<Split>();
		mSplits.add(mSplit);
		mSoundPlayer = ((AthleteApplication) getApplication())
				.getMediaPlayerVoice();
		trackRecordingServiceConnection = new TrackRecordingServiceConnection(
				this, bindChangedCallback);

		rawIDforTime = new HashMap<Integer, Integer>();
		setSecondsHashMap();
		SharedPreferences sharedPreferences = getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		sharedPreferences
				.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
		sharedPreferenceChangeListener.onSharedPreferenceChanged(
				sharedPreferences, null);
		baseShared = getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE);

		currenUser = userBL.getBy(baseShared.getString(
				Constants.SharedPreferencesKeys.CURRENT_ID, null));
		profileUser = baseOperationsBL.getFromDBByField(ProfileUser.class,
				ProfileUser.ID, currenUser.getProfileID());
		txtCountDown = (TextView) findViewById(R.id.txtCountDown);
		minRecordingDistance = PreferencesUtils.getInt(this,
				R.string.min_recording_distance_key,
				PreferencesUtils.MIN_RECORDING_DISTANCE_DEFAULT);
		minRequiredAccuracy = PreferencesUtils.getInt(this,
				R.string.min_required_accuracy_key,
				PreferencesUtils.MIN_REQUIRED_ACCURACY_DEFAULT);
		trackController = new TrackController(this,
				trackRecordingServiceConnection, true, recordListener,
				stopListener);
		txtNotifyCamera = (TextView) findViewById(R.id.txtNotifyCamera);
		isAutoPauseCheck = baseShared.getBoolean(
				Constants.SharedPreferencesKeys.AUTO_PAUSE, false);
		checkAndSetTrackHub();
		linearGPS = (LinearLayout) findViewById(R.id.linearGPS);
		isMetric = PreferencesUtils.getMetricUnit(this);

		if (isMetric) {
			ONE_VALUE = distanceForRunUpdate = baseShared.getFloat(
					com.athlete.Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
					com.athlete.Constants.AUDIO_TIMING_FLOAT.ONE);
		} else {
			ONE_VALUE = distanceForRunUpdate = baseShared.getFloat(
					com.athlete.Constants.INTENT_KEY.AUDIO_TIMING_MILE,
					com.athlete.Constants.AUDIO_TIMING_FLOAT.ZERO_POINT_FIVE);
		}

		setupTabs();
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
		findViewById(R.id.btnManual).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isStop) {
							if (trackDataHub != null) {
								trackDataHub.stop();
							}
							((AthleteApplication) getApplication())
									.setPathes(pathes.toArray(new String[pathes
											.size()]));
							trackRecordingServiceConnection.unbind();
							startActivityForResult(
									new Intent(TabActivityTrack.this,
											ActivityManual.class),
									com.athlete.Constants.RESULT_CODE_TRACK);
						}
					}
				});
		findViewById(R.id.linearCamera).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AnalyticsUtils.sendPageViews(TabActivityTrack.this,
								"TrackScreen", "Clicks", "button", "Camera", 0);
						addPictureMode();

					}
				});
		layoutGPSParams = new LinearLayout.LayoutParams(CommonHelper.getPX(8,
				TabActivityTrack.this), CommonHelper.getPX(13,
				TabActivityTrack.this));
		layoutGPSParams.setMargins(
				CommonHelper.getPX(1, TabActivityTrack.this), 0, 0, 0);

	}

	private void checkAndSetTrackHub() {
		trackDataHub = TrackDataHub.newInstance(TabActivityTrack.this);
		trackDataHub.start();
		trackDataHub.registerTrackDataListener(TabActivityTrack.this, EnumSet
				.of(TrackDataType.SELECTED_TRACK, TrackDataType.TRACKS_TABLE,
						TrackDataType.LOCATION, TrackDataType.PREFERENCE));

	}

	protected void setMapActivity(ActivityMapTrack activityMapTrack) {
		trackMapTrack = activityMapTrack;
	}

	protected void setActivitySplits(ActivitySplits activitySplits) {
		this.activitySplits = activitySplits;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (recordingTrackId != -1L && startNewRecording) {
			trackDataHub.loadTrack(recordingTrackId);
		}
		TrackRecordingServiceConnectionUtils.resumeConnection(this,trackRecordingServiceConnection);

		if (recordingTrackId != -1L && lastTripStatistics == null) {
			Track track = new Track();
			track = MyTracksProviderUtils.Factory.get(this).getTrack(
					recordingTrackId);
			lastTripStatistics = track.getTripStatistics();
		}
		trackController
				.update(recordingTrackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT,
						recordingTrackPaused,
						lastTripStatistics != null ? lastTripStatistics
								.getMovingTime() : 0);

	}

	public MediaPlayer getMediaPlayer() {
		return mSoundPlayer;

	}

	public boolean getStartNewRecording() {
		return startNewRecording;

	}

	public List<Split> getListOfSplit() {
		return mSplits;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == com.athlete.Constants.RESULT_CODE_TRACK
				&& requestCode == com.athlete.Constants.RESULT_CODE_TRACK) {
			if (!isNotification) {
				setResult(com.athlete.Constants.RESULT_CODE_TRACK, data);
				finish();
			} else {
				finish();
				startActivity(new Intent(TabActivityTrack.this,
						TabActivityMain.class).addFlags(
						Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						com.athlete.Constants.INTENT_KEY.BOOLEAN_VALUE, true));
			}

		}
		if (requestCode == CAMERA_REQUEST || requestCode == CAMERA_GALLERY) {
			if (isSelectedTrackRecording() && pauseAfterPhoto) {
				pauseAfterPhoto = false;
				paused2resume();
			}
		}
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			pathes.add(getCurrentPhotoPath());
			handleBigCameraPhoto();
			updateNotifyCamera();

		} else {
			if (requestCode == CAMERA_GALLERY && resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				selectedImage.getPath();
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				if (cursor == null) {
					return;
				}
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				if (filePath != null) {
					pathes.add(filePath);
					setCurrentPhotoPath(filePath);
				}
				handleBigCameraPhoto();
				updateNotifyCamera();

			}
		}
		if (resultCode == com.athlete.Constants.RESULT_CODE_TAB
				&& requestCode == com.athlete.Constants.RESULT_CODE_TRACK) {
			if (!isNotification) {
				setResult(com.athlete.Constants.RESULT_CODE_TAB);
				finish();
			} else {
				finish();
				startActivity(new Intent(TabActivityTrack.this,
						TabActivityMain.class).addFlags(
						Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						Constants.INTENT_KEY.BOOLEAN_VALUE, true));
			}

		}
	}

	private void updateNotifyCamera() {
		txtNotifyCamera.setVisibility(View.VISIBLE);
		txtNotifyCamera.setText(String.valueOf(pathes.size()));
	}

	private TripStatistics lastTripStatistics = null;
	private TripStatistics prevTrackStatistics = null;
	private final OnClickListener stopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			isStop = true;
			if (trackDataHub != null) {
				trackDataHub.stop();
			}
			if (mSoundPlayer != null) {
				mSoundPlayer.stop();
			}

			((AthleteApplication) getApplication()).setPathes(pathes
					.toArray(new String[pathes.size()]));
			TrackRecordingServiceConnectionUtils.stopRecording(
					TabActivityTrack.this, trackRecordingServiceConnection,
					true);
		}
	};
	private final OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences,String key) {
			if (key == null || key.equals(PreferencesUtils.getKey(TabActivityTrack.this, R.string.recording_track_id_key))) {
				recordingTrackId = PreferencesUtils.getLong(TabActivityTrack.this, R.string.recording_track_id_key);
				if (key != null) {
					boolean isRecording = recordingTrackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT;
					if (isRecording) {
						trackRecordingServiceConnection.startAndBind();
					}
					trackController.update(
							isRecording,
							recordingTrackPaused,
							lastTripStatistics != null ? lastTripStatistics.getMovingTime() : 0);
					return;
				}
			}
			if (key == null || key.equals(PreferencesUtils.getKey(TabActivityTrack.this,R.string.recording_track_paused_key))) {
				recordingTrackPaused = PreferencesUtils.getBoolean(
						TabActivityTrack.this,
						R.string.recording_track_paused_key,
						PreferencesUtils.RECORDING_TRACK_PAUSED_DEFAULT);
				if (key != null) {
					trackController
							.update(recordingTrackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT,
									recordingTrackPaused,
									lastTripStatistics != null ? lastTripStatistics
											.getMovingTime() : 0);
					return;
				}
			}
		}
	};
	private final OnClickListener recordListener = new OnClickListener() {
		public void onClick(View v) {
			isStop = false;
			if (recordingTrackId == PreferencesUtils.RECORDING_TRACK_ID_DEFAULT) {
				if (baseShared.getInt(com.athlete.Constants.INTENT_KEY.COUNTDOUNT, 0) == 0) {
					startRecording();
				} else {
					startCountDown();
				}
				AnalyticsUtils.sendPageViews(TabActivityTrack.this,
						AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.TRACK_SCREEN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
						AnalyticsUtils.GOOGLE_ANALYTICS.START_TRACK, 0);
			} else {
				if (recordingTrackPaused) {
					paused2resume();
				} else {
					// Recording -> Paused
					recording2paused();
				}
			}
		}
	};

	private void startRecording() {

		startNewRecording = true;
		trackRecordingServiceConnection.startAndBind();
		bindChangedCallback.run();
	}

	public TrackDataHub getTrackDataHub() {

		return trackDataHub;
	}

	public boolean getTrackPaused() {
		return recordingTrackPaused;
	}

	public HashMap<Integer, Integer> getHashRawID() {
		return rawIDforTime;
	}

	public TripStatistics getTripStatistics() {
		return lastTripStatistics;
	}

	private void startCountDown() {
		PlaySound.playSound(TabActivityTrack.this, mSoundPlayer,
				R.raw.trackingbegin, true);
		findViewById(R.id.linearTabMain).setVisibility(View.GONE);
		txtCountDown.setVisibility(View.VISIBLE);
	}

	private void setupTabs() {
		tabs = getTabHost();
		View indicator;
		TabHost.TabSpec tab = tabs.newTabSpec("1");
		Intent intent = new Intent(TabActivityTrack.this,
				com.athlete.activity.track.ActivityMapTrack.class);

		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_route,
				getString(R.string.tab_route_txt));
		tab.setIndicator(indicator);
		tabs.addTab(tab);
		tab = tabs.newTabSpec("2");
		intent = new Intent(this, ActivitySplits.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_splits,
				getString(R.string.tab_splits_txt));
		tab.setIndicator(indicator);
		tabs.addTab(tab);

		tab = tabs.newTabSpec("3");
		intent = new Intent(this, MockGpsProviderActivity.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_splits,
				getString(R.string.run_lower));
		indicator.setVisibility(View.INVISIBLE);
		tab.setIndicator(indicator);
		tabs.addTab(tab);

		tab = tabs.newTabSpec("4");
		intent = new Intent(this, MockGpsProviderActivity.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_camera,
				getString(R.string.tab_camera_txt));
		tab.setIndicator(indicator);
		indicator.setVisibility(View.INVISIBLE);
		tabs.addTab(tab);
		tab = tabs.newTabSpec("5");
		intent = new Intent(this, ActivityMusic.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_music,
				getString(R.string.tab_music_txt));
		tab.setIndicator(indicator);
		tabs.addTab(tab);
		tabs.setCurrentTab(0);
	}

	private View prepareTabView(int drawableIcon, String text) {
		View view = getLayoutInflater().inflate(R.layout.tab, null);
		((ImageView) view.findViewById(R.id.imViewTab))
				.setImageResource(drawableIcon);
		((TextView) view.findViewById(R.id.tvTab)).setText(text);
		view.findViewById(R.id.txtNotifyMsg).setVisibility(View.GONE);
		return view;
	}

	private final Runnable bindChangedCallback = new Runnable() {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					trackController
							.update(recordingTrackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT,
									recordingTrackPaused, 0);
				}
			});

			if (!startNewRecording) {
				return;
			}

			ITrackRecordingService service = trackRecordingServiceConnection.getServiceIfBound();
			if (service == null) {
				Log.d(Constants.TAG,
						"service not available to start a new recording");
				return;
			}
			try {
				recordingTrackId = service.startNewTrack();
				startNewRecording = false;
				trackDataHub.loadTrack(recordingTrackId);

				trackDataHub.registerTrackDataListener(TabActivityTrack.this,
						EnumSet.of(TrackDataType.SELECTED_TRACK,
								TrackDataType.TRACKS_TABLE,
								TrackDataType.LOCATION,
								TrackDataType.PREFERENCE));
				TrackRecordingServiceConnectionUtils.resumeConnection(
						TabActivityTrack.this, trackRecordingServiceConnection);
				tabs.setCurrentTab(0);

				Toast.makeText(TabActivityTrack.this,
						R.string.track_list_record_success, Toast.LENGTH_SHORT)
						.show();

				PlaySound.playSound(TabActivityTrack.this, mSoundPlayer,
						R.raw.trackingstarted, false);

			} catch (Exception e) {

				Toast.makeText(TabActivityTrack.this,
						R.string.track_list_record_error, Toast.LENGTH_LONG)
						.show();

			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		trackRecordingServiceConnection.unbind();
		if (trackDataHub != null) {
			trackDataHub.stop();
		}
		// unregisterReceiver(unlockdone);
	}

	@Override
	public void onLocationStateChanged(LocationState locationState) {
		if (isSelectedTrackRecording() && isTrackStartedFinish) {
			Log.d("locationState", locationState.toString());
			if (locationState == LocationState.DISABLED
					|| locationState == LocationState.NO_FIX) {
				if (!isNoFix) {
					isNoFix = true;
					PlaySound.playSound(TabActivityTrack.this, mSoundPlayer,
							R.raw.gpssignallost, false);
					startTimerLostSignal();
				}
			} else {
				if (isNoFix) {
					isNoFix = false;
					if (lostTime >= timeOutForGPSFOUND) {
						PlaySound.playSound(TabActivityTrack.this,
								mSoundPlayer, R.raw.gpssignalfound, false);
					}
					mTimerLostSignal.cancel();
					lostTime = 0;
				}
			}
		}
		if (locationState != LocationState.GOOD_FIX) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					lastLocation = null;
					if (!isSelectedTrackRecording()) {
						isNoFix = true;
					}
					Log.d("locationState", "locationState!=GOOD");
				}
			});
		}

		if (!isSelectedTrackRecording()) {
			message = null;

		} else {
			switch (locationState) {
			case DISABLED:
				String setting = getString(GoogleLocationUtils
						.isAvailable(TabActivityTrack.this) ? R.string.gps_google_location_settings
						: R.string.gps_location_access);
				message = getString(R.string.gps_disabled, setting);

				break;
			case NO_FIX:
				message = getString(R.string.gps_wait_for_signal);

				break;
			case BAD_FIX:
			case GOOD_FIX:
				message = null;

				break;
			default:
				break;
			}

			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (message == null) {
						messageTextView.setVisibility(View.GONE);
						return;
					}
					messageTextView.setText(message);
					messageTextView.setVisibility(View.VISIBLE);

				}
			});
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (location != null) {
			this.runOnUiThread(new Runnable() {
				@SuppressLint("NewApi")
				@Override
				public void run() {
					if (lastLocationPause != null) {
						double distanceToLastTrackLocation = location
								.distanceTo(lastLocationPause);
						if (isAutopause
								&& distanceToLastTrackLocation >= minRecordingDistance
								&& location.getAccuracy() <= minRequiredAccuracy) {
							findViewById(R.id.run).callOnClick();
							isAutopause = false;
						}
					}
					if (isSelectedTrackRecording() && !isSelectedTrackPaused()) {
						lastLocation = location;
					} else {
						if (lastLocation != null) {
							lastLocation = null;
						}
					}
					setAccuratedGPSTitle((int) location.getAccuracy());
				}
			});
			if (mSecond >= timeOutForPause) {
				mSecond = 0;
				isAutopause = true;
				findViewById(R.id.run).callOnClick();
			}
		}
	}

	private synchronized void setAccuratedGPSTitle(int accuracy) {
		if (accuracy > accurate) {
			if (countGPSPosition != 0) {
				countGPSPosition = 0;
				linearGPS.removeAllViews();
			}
			return;
		}
		for (int i = 1; i <= countOfSectors; i++) {
			if (accuracy > oneSector * (countOfSectors - i)
					&& accuracy <= (oneSector * (countOfSectors - i))
							+ oneSector) {
				if (countGPSPosition != i) {
					countGPSPosition = i;
					changeGPS();
					break;
				}
			}
		}
	}

	private void changeGPS() {
		linearGPS.removeAllViews();
		for (int i = 0; i < countGPSPosition; i++) {
			LinearLayout im = new LinearLayout(this);
			im.setLayoutParams(layoutGPSParams);
			im.setBackgroundResource(R.drawable.gps_bar);
			linearGPS.addView(im);
		}
	}

	private void updateUi(TabActivityTrack activity) {
		double weight = weightDeff;
		if (profileUser != null && profileUser.getWeightUnit() != null) {
			weight = profileUser.getWeight();
			if (!profileUser.getWeightUnit().equalsIgnoreCase(
					com.athlete.Constants.WEIGHT_UNIT.POUNDS)) {
				weight *= UnitConversions.KG_TO_POUNDS;
			}
		}
		if (weight == 0) {
			weight = weightDeff;
		}
		StatsUtils.setTripStatisticsValues(activity, lastTripStatistics, weight);
		
	}

	@Override
	public void onHeadingChanged(double heading) {
	}

	@Override
	public void onSelectedTrackChanged(Track track) {
	}

	@Override
	public void onTrackUpdated(final Track track) {
		/*this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lastTripStatistics = track != null ? track.getTripStatistics()
						: null;
				lastLocationPause = lastLocation;
				if (lastLocation != null) {
					if (firstElevation == 0) {
						firstElevation = lastTripStatistics.getCurrentElevation();
					}
					boolean isWaypoint = trackMapTrack.showMarker(
							lastTripStatistics, prevTrackStatistics,
							prevLocation, lastLocation);
					prevTrackStatistics = new TripStatistics(lastTripStatistics);
					prevLocation = new Location(lastLocation);
					startPlayRunUpdate();
					// add splits
					if (!recordingTrackPaused) {
						if (isWaypoint) {
							mSplit.setClimb(lastTripStatistics
									.getCurrentElevation() - firstElevation);
							firstElevation = lastTripStatistics
									.getCurrentElevation();
							mSplit = new Split();
							prevMaxMovingTime = lastTripStatistics
									.getMovingTime();
							prevMaxTotalDistance = lastTripStatistics
									.getTotalDistance();
							mSplits.add(0, mSplit);
						}
						setSplit();
						if (activitySplits != null) {
							activitySplits.updateSplits();
						}
					}
				}
				if (track != null
						&& recordingTrackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT
						&& !recordingTrackPaused) {
					// Start or restart autopause timer
					if (isAutoPauseCheck) {
						startTimer();
					}
				}
				updateUi(TabActivityTrack.this);
			}
		});*/

	}

	private void setSplit() {
		//mSplit.setClimb(lastTripStatistics.getCurrentElevation() - firstElevation);
		mSplit.setClimb(lastTripStatistics.getMinElevation() - firstElevation);
		double distance = lastTripStatistics.getTotalDistance()
				- prevMaxTotalDistance;
		long movingTime = lastTripStatistics.getMovingTime()
				- prevMaxMovingTime;
		double speed = distance / ((double) movingTime / Constants.ONE_THSND);
		speed *= UnitConversions.MS_TO_KMH;
		if (!isMetric) {
			speed *= UnitConversions.KM_TO_MI;
		}
		double pace = speed == 0 ? 0.0 : oneMinute / speed;
		pace = (int) pace + ((int) ((pace - (int) pace) * oneMinute))
				/ double100;
		mSplit.setAvgPace(pace);

	}

	private void startPlayRunUpdate() {

		TripStatistics trackStatisticForPlayer = new TripStatistics(
				lastTripStatistics);
		double lastDistance = isMetric ? lastTripStatistics.getTotalDistance()
				/ Constants.ONE_THSND : lastTripStatistics.getTotalDistance()
				* UnitConversions.M_TO_MI;
		if (lastDistance >= distanceForRunUpdate && distanceForRunUpdate != -1) {
			if (isMetric) {
				trackStatisticForPlayer.setTotalDistance(distanceForRunUpdate
						* Constants.ONE_THSND);
			} else {
				trackStatisticForPlayer.setTotalDistance(distanceForRunUpdate
						* UnitConversions.MI_TO_M);
			}
			distanceForRunUpdate += ONE_VALUE;
			PlaySound.runCompletePlay(this, mSoundPlayer,
					trackStatisticForPlayer, mSplit, rawIDforTime, false);

		}

	}

	private void startTimer() {
		mTimerAutoPause.cancel();
		mSecond = 0;
		mTimerAutoPause = new Timer();
		mTimerAutoPause.schedule(new TimerTask() {
			@Override
			public void run() {
				if (recordingTrackPaused) {
					mTimerAutoPause.cancel();
				}
				mSecond++;
			}
		}, Constants.ONE_SECOND, Constants.ONE_SECOND);

	}

	private void startTimerLostSignal() {
		mTimerLostSignal.cancel();
		lostTime = 0;
		mTimerLostSignal = new Timer();
		mTimerLostSignal.schedule(new TimerTask() {
			@Override
			public void run() {

				lostTime++;
			}
		}, Constants.ONE_SECOND, Constants.ONE_SECOND);

	}

	public void startCountDownTimer(int second) {
		mTimerCountDown.cancel();
		time = second;
		txtCountDown.setText(String.valueOf(time));
		synchronized (mTimerCountDown) {
			mTimerCountDown = new Timer();
			mTimerCountDown.schedule(new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							if (time == 0) {
								mTimerCountDown.cancel();
								findViewById(R.id.linearTabMain).setVisibility(
										View.VISIBLE);
								txtCountDown.setVisibility(View.GONE);
								startRecording();
								return;
							}
							if (time <= countDown5) {
								PlaySound.playSound(TabActivityTrack.this,
										mSoundPlayer, rawIDforTime.get(time),
										false);
							}
							if (time == countDown30 || time == countDown10) {
								int[] sounds = { rawIDforTime.get(time),
										R.raw.seconds };
								PlaySound.playSound(TabActivityTrack.this,
										mSoundPlayer, sounds);
							}

							txtCountDown.setText(String.valueOf(time));
							time--;
						}

					});
				}
			}, Constants.ONE_SECOND, Constants.ONE_SECOND);
		}
	}

	@Override
	public void clearTrackPoints() {
	}

	@Override
	public void onSampledInTrackPoint(Location location) {
	}

	@Override
	public void onSampledOutTrackPoint(Location location) {

	}

	@Override
	public void onSegmentSplit(Location location) {

	}

	@Override
	public void onNewTrackPointsDone() {

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

	private void setSecondsHashMap() {
		for (int i = 1; i <= countSecondRaw; i++) {
			rawIDforTime.put(i, TabActivityTrack.this.getResources()
					.getIdentifier("n" + i, defType, this.getPackageName()));
		}
		for (int i = startForRaws; i <= countSecondRawMore100; i += countSecondRaw) {
			rawIDforTime.put(i, TabActivityTrack.this.getResources()
					.getIdentifier("n" + i, defType, this.getPackageName()));
		}
	}
	@Override
	public void clearWaypoints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewWaypoint(Waypoint waypoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewWaypointsDone() {
		// TODO Auto-generated method stub
		
	}

}
