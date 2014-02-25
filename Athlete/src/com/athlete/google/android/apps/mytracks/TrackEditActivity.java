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

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.track.BaseSaveTrackActivity;
import com.athlete.control.PlaySound;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.io.file.SaveActivity;
import com.athlete.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.athlete.google.android.apps.mytracks.stats.TripStatistics;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.model.ActivityType;
import com.athlete.model.IdleWorkOut;
import com.athlete.util.AnalyticsUtils;

/**
 * An activity that let's the user see and edit the user editable track meta
 * data such as track name, activity type, and track description.
 * 
 * @author Leif Hendrik Wilden
 */
public class TrackEditActivity extends BaseSaveTrackActivity {
	private final String defType = "raw";
	private HashMap<Integer, Integer> rawIDforTime;
	private MediaPlayer mediaPlayer;
	private final int rawCountTimes = 100;
	private final int rawCountTimes900 = 900;
	private final int rawOffsetTime = 200;
	private boolean isSave = false;

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(TrackEditActivity.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SAVE);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.track_edit);
		isSave = false;
		trackId = getIntent().getLongExtra(EXTRA_TRACK_ID, -1L);
		if (trackId == -1L) {
			Log.e(TAG, "invalid trackId");
			finish();
			return;
		}
        workoutActivityType = ActivityType.getDefaultActivityType();
		sp = getShared();
		myTracksProviderUtils = MyTracksProviderUtils.Factory.get(this);
		track = myTracksProviderUtils.getTrack(trackId);

		if (track == null) {
			Log.e(TAG, "No track for " + trackId);
			finish();
			return;
		}

		name = (EditText) findViewById(R.id.track_edit_name);
		checkFB = (CheckBox) findViewById(R.id.checkBoxFb);
		track.setName("");
		mTxtTitle = (TextView) findViewById(R.id.txtTitle);

		//track.setPrivacy(getString(R.string.label_public));
        mTxtActivityType = (TextView) findViewById(R.id.txtActivityType);
        mTxtActivitySubtype = (TextView) findViewById(R.id.txtActivitySubtype);
		mTxtRouteView = (TextView) findViewById(R.id.txtRouteView);

		mediaPlayer = ((AthleteApplication) getApplication())
				.getMediaPlayerVoice();
		setSecondsHashMap();
		if (track.getTripStatistics().getTotalDistance() > 0
				&& track.getTripStatistics().getMovingTime() > 0) {
			PlaySound.setStop(false);
			PlaySound.runCompletePlay(this, mediaPlayer,
					track.getTripStatistics(), null, rawIDforTime, true);
		} else {
			PlaySound.setStop(false);
			PlaySound.playSound(this, mediaPlayer, R.raw.trackingcomplete,
					false);
		}
		setTitle();
        findViewById(R.id.activityType).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAdapterForDialog();
                    }
                });
        findViewById(R.id.activitySubType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialogForActivitySubtype();
            }
        });
		findViewById(R.id.btnDisacard).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						alertDialog();
					}
				});
		findViewById(R.id.runPrivace).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						setAdapterForDialogPrivacy();
					}
				});
		description = (EditText) findViewById(R.id.track_edit_description);
		description.setText(track.getDescription());

		final Button save = (Button) findViewById(R.id.track_edit_save);

		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				hideKeyboard(TrackEditActivity.this);
				if (!isSave) {
					if (track.getNumberOfPoints() > 1
							&& track.getTripStatistics().getMovingTime() > 0
							&& track.getTripStatistics().getTotalDistance() > 0) {
						isSave = true;

						track.setName(name.getText().toString());
						track.setDescription(description.getText().toString());
                       // track.setActivityType(mTxtActivityType.getText().toString());
                       // track.setActivitySubType(mTxtActivitySubtype.getText().toString());
						if (checkFB.isChecked() && mFBAcces != null) {
							//track.setAcces(mFBAcces);
						}
						myTracksProviderUtils.updateTrack(track);
						playSaveTrack();

					} else {
						Toast.makeText(TrackEditActivity.this,
								getString(R.string.toast_1_point),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		checkFB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {

					mFBAcces = sp.getString(
							Constants.SharedPreferencesKeys.FB_ACCES, null);
					if (mFBAcces == null) {
						callFBAuth();
					}

				}
			}
		});

	}

	private void alertDialog() {
		hideKeyboard(TrackEditActivity.this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_discard))
				.setMessage(getString(R.string.message_discard))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mediaPlayer != null) {
									PlaySound.setStop(true);
									mediaPlayer.stop();
									mediaPlayer = null;
								}

								setResult(com.athlete.Constants.RESULT_CODE_TRACK);

								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}

	private void setSecondsHashMap() {
		rawIDforTime = new HashMap<Integer, Integer>();
		for (int i = 1; i <= rawCountTimes; i++) {
			rawIDforTime.put(i, TrackEditActivity.this.getResources()
					.getIdentifier("n" + i, defType, this.getPackageName()));
		}
		for (int i = rawOffsetTime; i <= rawCountTimes900; i += rawCountTimes) {
			rawIDforTime.put(i, TrackEditActivity.this.getResources()
					.getIdentifier("n" + i, defType, this.getPackageName()));
		}
	}

	private void startSaveActivity(TrackFileFormat trackFileFormat) {
		((AthleteApplication) getApplication()).getMediaPlayerMusic().stop();
		Intent intent = new Intent(this, SaveActivity.class).putExtra(
				SaveActivity.EXTRA_TRACK_ID, trackId).putExtra(
				SaveActivity.EXTRA_TRACK_FILE_FORMAT,
				(Parcelable) trackFileFormat);
		startActivityForResult(intent, Constants.RESULT_CODE_TRACK);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == com.athlete.Constants.RESULT_CODE_TRACK
				&& requestCode == com.athlete.Constants.RESULT_CODE_TRACK) {
			IdleWorkOut idleWorkOut = new IdleWorkOut();
			idleWorkOut.setDistance(track.getTripStatistics()
					.getTotalDistance());
			idleWorkOut
					.setDuration(track.getTripStatistics().getMovingTime() / 1000);

			idleWorkOut.setTitle(name.getText().toString());
			idleWorkOut.setPostBody(description.getText().toString());

            idleWorkOut.setActivityType(mTxtActivityType.getText().toString());
            idleWorkOut.setActivitySubType(mTxtActivitySubtype.getText().toString());

			idleWorkOut.setPrivacy(mTxtRouteView.getText().toString()
					.toLowerCase());
			idleWorkOut.setFbAcces(mFBAcces);
			idleWorkOut.setPhotoPath(((AthleteApplication) getApplication())
					.getPathes());
			idleWorkOut.setTrackPath(data
					.getStringExtra(Constants.INTENT_KEY.TRACK_PATH));
			idleWorkOut.setIdUser(getUserID());

			baseBl.createOrUpdate(IdleWorkOut.class, idleWorkOut);
			setResult(com.athlete.Constants.RESULT_CODE_TRACK, data);
			finish();
		}
	}

	private void setTitle() {

		TripStatistics tripStatistics = track.getTripStatistics();
		boolean metricUnits = PreferencesUtils
				.getMetricUnit(TrackEditActivity.this);
		double totalDistance = tripStatistics == null ? Double.NaN
				: tripStatistics.getTotalDistance();
		Pair<String, String> distance = StringUtils.formatDistance(
				TrackEditActivity.this, totalDistance, metricUnits, false);
		if (totalDistance > 0 && tripStatistics.getMovingTime() > 0) {
			mTxtTitle.setText(distance.first
					+ " "
					+ distance.second
					+ " / "
					+ StringUtils.formatElapsedTime(tripStatistics
							.getMovingTime()));
		} else {
			mTxtTitle.setText("");
		}
	}

	@Override
	public void onBackPressed() {
		alertDialog();
	}

	private void playSaveTrack() {
		PlaySound.playSound(TrackEditActivity.this, mediaPlayer,
				R.raw.trackingsaved, false);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				PlaySound.setStop(true);
				startSaveActivity(TrackFileFormat.GPX);
			}
		});
	}

	private void callFBAuth() {
		mFBClick = true;
		isSaveTrack = true;

		mFacebook.authorize(TrackEditActivity.this, Constants.PERMS,
				new LoginDialogListener());
	}

}
