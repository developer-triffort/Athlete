/*
 * Copyright 2012 Google Inc.
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

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.athlete.R;
import com.athlete.activity.track.TabActivityTrack;
import com.athlete.control.PlaySound;
import com.athlete.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.model.Split;

/**
 * Track controller for record, pause, resume, and stop.
 * 
 * @author Jimmy Shih
 */
public class TrackController {

	private static final int ONE_SECOND = 1000;

	private final Activity activity;
	private final Handler handler;
	private final Handler handlerPause;
	private final View containerView;
	private final TextView totalTimeTextView;
	private final ImageButton recordImageButton;
	private final Button stopImageButton, resumeButton;
	private final boolean alwaysShow;
	private final View titleBar;
	private boolean isRecording;
	private boolean isPaused;
	private long totalTime = 0;
	private long totalTimePause = 0;
	private long pauseNow = 0;
	private long pauseTimeTimestamp = 0;
	private String value;
	private long totalTimeTimestamp = 0;
	// A runnable to update the total time.
	private final Runnable updateTotalTimeRunnable = new Runnable() {
		public void run() {
			if (isRecording && !isPaused) {
				displayTime();
				handler.postDelayed(this, ONE_SECOND);
			}
		}
	};

	private void displayTime() {
		value = StringUtils.formatElapsedTimeWithHour(System
				.currentTimeMillis() - totalTimeTimestamp - totalTimePause);
		String valuesArr[] = value.split(":");
		StringBuffer buffer = new StringBuffer();
		if (valuesArr.length > 2) {
			if (valuesArr[0].equalsIgnoreCase("0")) {

				buffer.append(valuesArr[0] + ":");
				if (valuesArr[1].equalsIgnoreCase("00")) {
					buffer.append(valuesArr[1] + ":");
					buffer.append("<font color='#000000'>" + valuesArr[2]
							+ "</font>");
				} else {
					buffer.append("<font color='#000000'>" + valuesArr[1] + ":"
							+ valuesArr[2] + "</font>");
				}
			} else {
				totalTimeTextView.setTextColor(Color.BLACK);
				buffer.append(value);
			}
		} else {
			buffer.append(value);
		}

		totalTimeTextView.setText(Html.fromHtml(buffer.toString()));
	}

	private final Runnable updateTimePauseRunnable = new Runnable() {
		public void run() {
			if (isRecording && isPaused) {
				pauseNow = System.currentTimeMillis() - pauseTimeTimestamp;

				handlerPause.postDelayed(this, ONE_SECOND);
			}
		}
	};

	public TrackController(final Activity activity,
			TrackRecordingServiceConnection trackRecordingServiceConnection,
			boolean alwaysShow, OnClickListener recordListener,
			OnClickListener stopListener) {
		this.activity = activity;
		this.alwaysShow = alwaysShow;
		handler = new Handler();
		handlerPause = new Handler();
		containerView = (View) activity.findViewById(R.id.linearPause);
		titleBar = (View) activity.findViewById(R.id.titleBar);
		containerView.setVisibility(View.GONE);

		totalTimeTextView = (TextView) activity.findViewById(R.id.txtTime);
		if (totalTime == 0) {
			totalTimeTextView.setText(Html
					.fromHtml("0:00:<font color='#000000'>00</font>"));
		}
		recordImageButton = (ImageButton) activity.findViewById(R.id.run);
		resumeButton = (Button) activity.findViewById(R.id.btnResume);
		recordImageButton.setOnClickListener(recordListener);
		resumeButton.setOnClickListener(recordListener);
		stopImageButton = (Button) activity.findViewById(R.id.btnFinish);
		stopImageButton.setOnClickListener(stopListener);
		final TabActivityTrack tabActivityTrack = (TabActivityTrack) activity;
		activity.findViewById(R.id.linearData).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
				
							if (tabActivityTrack != null&&isRecording
									&& tabActivityTrack.getTripStatistics()
											.getTotalDistance() > 0) {
								Split split = tabActivityTrack.getListOfSplit()
										.get(tabActivityTrack.getListOfSplit()
												.size() - 1);
								PlaySound.runCompletePlay(activity,
										tabActivityTrack.getMediaPlayer(),
										tabActivityTrack.getTripStatistics(),
										split, tabActivityTrack.getHashRawID(),
										false);

							}
					}
				});
	}

	public void update(boolean recording, boolean paused, long movingTime) {
		isRecording = recording;
		isPaused = paused;
		if (recording && !paused) {
			containerView.setVisibility(View.GONE);
		}
		if (recording && paused) {
			containerView.setVisibility(View.VISIBLE);
		}

		if (!alwaysShow && !isRecording) {
			stop();
			return;
		}
		if (isRecording && isPaused) {
			if (totalTimeTimestamp != 0) {
				displayTime();
			}
			pauseTimeTimestamp = System.currentTimeMillis();
			handlerPause.postDelayed(updateTimePauseRunnable, ONE_SECOND);
		} else {
			totalTimePause += pauseNow;
			pauseNow = 0;
		}
		recordImageButton
				.setImageResource(isRecording && !isPaused ? R.drawable.button_pause
						: R.drawable.button_play);

		titleBar.setVisibility(isRecording ? View.GONE : View.VISIBLE);

		recordImageButton.setContentDescription(activity.getString(isRecording
				&& !isPaused ? R.string.menu_pause_track
				: R.string.menu_record_track));
		stopImageButton.setEnabled(isRecording);

		stop();
		if (isRecording && !isPaused) {

			if (totalTimeTimestamp == 0) {
				totalTimeTimestamp = System.currentTimeMillis();
			} else {
				displayTime();
			}
			handler.postDelayed(updateTotalTimeRunnable, ONE_SECOND);
		}
	}

	/**
	 * Stops the timer.
	 */
	public void stop() {
		handler.removeCallbacks(updateTotalTimeRunnable);
	}

}
