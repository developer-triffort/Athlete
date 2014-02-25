package com.athlete.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.track.TabActivityTrack;
import com.athlete.google.android.apps.mytracks.stats.TripStatistics;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.model.Split;
import com.athlete.util.AudioFocusHelper;

public class PlaySound {
	private static Set<Integer> timePauseID;
	private static boolean mStop = false;
	private static AudioFocusHelper mAudioFocusHelper;
	private static final int fractionHungred = 100;
	private static final int halfSecond = 500;

	public static void setStop(boolean stop) {
		stopAudioFocus();
		mStop = stop;
	}

	private static Activity mActivity;

	public static void playSound(Activity activity, MediaPlayer mSoundPlayer,
			final int[] sound) {

		mActivity = activity;

		if (mSoundPlayer != null && mSoundPlayer.isPlaying()) {
			mSoundPlayer.stop();
			setAudioFocusHelper(mSoundPlayer, activity, sound, 0);
			return;
		}
		setAudioFocusHelper(mSoundPlayer, activity, sound, 0);

	}

	public static void playSound(Activity activity, MediaPlayer mSoundPlayer,
			final int sound, boolean isCountDelay) {

		mActivity = activity;

		if (mSoundPlayer != null && mSoundPlayer.isPlaying()) {
			mSoundPlayer.stop();
			setAudioFocusHelper(mSoundPlayer, activity, sound, 0, isCountDelay);
			return;
		}
		setAudioFocusHelper(mSoundPlayer, activity, sound, 0, isCountDelay);
	}

	public static void playSound(Activity activity, MediaPlayer mSoundPlayer,
			final int sound, boolean isCountDelay, Boolean isTrackStartedFinish) {

		playSound(activity, mSoundPlayer, sound, isCountDelay);

	}

	public static void runCompletePlay(final Activity activity,
			final MediaPlayer mSoundPlayer, TripStatistics tripStatistics,
			Split split, HashMap<Integer, Integer> rawSecond,
			boolean isRunComplete) {
		if (mSoundPlayer != null) {
			mSoundPlayer.stop();
		}
		mActivity = activity;
		String strTime = new String();
		timePauseID = new LinkedHashSet<Integer>();
		boolean metricUnits = PreferencesUtils.getMetricUnit(activity);
		// DISTANCE
		ArrayList<Integer> arraySound = new ArrayList<Integer>();
		if (isRunComplete) {
			arraySound.add(R.raw.trackingcomplete);
		}
		arraySound.add(R.raw.distance);

		double distance = Double.valueOf(StringUtils.formatDistance(activity,
				tripStatistics.getTotalDistance(), metricUnits, true).first);
		int fraction = (int) ((distance - (int) distance) * fractionHungred);
		if ((int) distance != 0) {
			arraySound.add(rawSecond.get((int) distance));
		}
		if (fraction != 0) {
			arraySound.add(R.raw.point);
			strTime = String.valueOf(fraction);
			if (strTime.length() == 1) {
				arraySound.add(R.raw.zero);
				arraySound.add(rawSecond.get(fraction));
			} else {
				arraySound.add(rawSecond.get(Integer.valueOf(strTime.substring(
						0, 1))));
				if (!strTime.substring(1, 2).equals("0")) {
					arraySound.add(rawSecond.get(Integer.valueOf(strTime
							.substring(1, 2))));
				}
			}
		}
		// KM or Mile
		if (metricUnits) {
			if (fraction == 0 && (int) distance == 1) {
				arraySound.add(R.raw.kilometer);
			} else {
				arraySound.add(R.raw.kilometers);
			}
		} else {
			if (fraction == 0 && (int) distance == 1) {
				arraySound.add(R.raw.mile);
			} else {
				arraySound.add(R.raw.miles);
			}
		}
		// PAUSE
		timePauseID.add(arraySound.size() - 1);
		// DURATION
		arraySound.add(R.raw.duration);
		long time = tripStatistics.getMovingTime();
		String value = StringUtils.formatElapsedTimeWithHour(time);
		String[] times = value.split(":");
		// Hour
		if (times[0] != null && !times[0].equals("0")) {
			arraySound.add(rawSecond.get(Integer.valueOf(times[0])));
			if (times[0].equals("1")) {
				arraySound.add(R.raw.hour);
			} else {
				arraySound.add(R.raw.hours);
			}
		}
		// Minute
		if (times[1] != null && !times[1].equals("00")) {
			arraySound.add(rawSecond.get(Integer.valueOf(times[1])));
			if (times[1].equals("01")) {
				arraySound.add(R.raw.minute);
			} else {
				arraySound.add(R.raw.minutes);
			}
		}

		// Second
		if (times[2] != null && !times[2].equals("00")) {
			arraySound.add(rawSecond.get(Integer.valueOf(times[2])));
			if (times[2].equals("01")) {
				arraySound.add(R.raw.second);
			} else {
				arraySound.add(R.raw.seconds);
			}
		}

		// PAUSE
		timePauseID.add(arraySound.size() - 1);
		// avg pace
		arraySound.add(R.raw.averagepace);

		String[] averagePace = StringUtils.formatSpeed(activity,
				tripStatistics.getAverageMovingSpeed(), metricUnits, false)
				.split("'");
		// Minute
		if (averagePace[0] != null && !averagePace[0].equals("0")) {
			arraySound.add(rawSecond.get(Integer.valueOf(averagePace[0])));
			if (averagePace[0].equals("01")) {
				arraySound.add(R.raw.minute);
			} else {
				arraySound.add(R.raw.minutes);
			}
		}

		// Second
		if (averagePace[1] != null && !averagePace[1].equals("00")) {
			arraySound.add(rawSecond.get(Integer.valueOf(averagePace[1])));
			if (averagePace[1].equals("01")) {
				arraySound.add(R.raw.second);
			} else {
				arraySound.add(R.raw.seconds);
			}
		}
		if (metricUnits) {
			arraySound.add(R.raw.perkilometer);
		} else {
			arraySound.add(R.raw.permile);
		}

		if (!isRunComplete && split != null) {
			// PAUSE
			timePauseID.add(arraySound.size() - 1);
			// current split pace
			arraySound.add(R.raw.currentsplitpace);
			double avgPace = split.getAvgPace();
			int fractionAvgPace = (int) ((avgPace - (int) avgPace) * 100);
			if ((int) avgPace != 0) {
				arraySound.add(rawSecond.get((int) avgPace));
				if (avgPace == 1) {
					arraySound.add(R.raw.minute);
				} else {
					arraySound.add(R.raw.minutes);
				}
			}

			// second
			if (fractionAvgPace != 0) {
				arraySound.add(rawSecond.get(fractionAvgPace));
				if (fractionAvgPace == 1) {
					arraySound.add(R.raw.second);
				} else {
					arraySound.add(R.raw.seconds);
				}
			}
			if (metricUnits) {
				arraySound.add(R.raw.perkilometer);
			} else
				arraySound.add(R.raw.permile);

		}
		int[] sound = new int[arraySound.size()];
		int i = 0;
		for (Integer integer : arraySound) {
			if (integer == null) {
				break;
			}
			sound[i++] = integer;
		}

		if (mSoundPlayer != null && mSoundPlayer.isPlaying()) {
			mSoundPlayer.stop();
			setAudioFocusHelper(mSoundPlayer, activity, sound, 0);

			return;
		}
		setAudioFocusHelper(mSoundPlayer, activity, sound, 0);

	}

	private static void setAudioFocusHelper(MediaPlayer mSoundPlayer,
			Activity activity, int sound, int index, boolean isCountDown) {

		stopAudioFocus();
		if (android.os.Build.VERSION.SDK_INT >= 8) {
			mAudioFocusHelper = new AudioFocusHelper(mActivity, mSoundPlayer,true);
			mAudioFocusHelper.requestFocusMayDuck();
			startPlay(activity, mSoundPlayer, sound, index, isCountDown);
		} else {
			mAudioFocusHelper = null;
		}
	}

	private static void setAudioFocusHelper(MediaPlayer mSoundPlayer,
			Activity activity, int[] sound, int index) {

		stopAudioFocus();
		if (android.os.Build.VERSION.SDK_INT >= 8) {

			mAudioFocusHelper = new AudioFocusHelper(mActivity, mSoundPlayer,true);
			mAudioFocusHelper.requestFocusMayDuck();
			startPlay(activity, mSoundPlayer, sound, index);
		} else {
			mAudioFocusHelper = null;
		}
	}

	private static void stopAudioFocus() {
		if (mAudioFocusHelper != null) {
			mAudioFocusHelper.abandonFocus();
		}
	}

	private static void startPlay(final Context ctx,
			final MediaPlayer mSoundPlayer, final int[] sound, final int index) {
		final int i = index + 1;
		if (!mAudioFocusHelper.requestFocusMayDuck()) {
			return;
		}
		if (sound[index] == 0 || mSoundPlayer == null || mStop) {
			mStop = false;
			stopAudioFocus();
			return;
		}

		Thread t = new Thread() {
			public void run() {
				AssetFileDescriptor afd = ctx.getResources().openRawResourceFd(
						sound[index]);
				try {
					mSoundPlayer.reset();
					mSoundPlayer.setDataSource(afd.getFileDescriptor(),
							afd.getStartOffset(), afd.getDeclaredLength());
					mSoundPlayer.prepare();

					mSoundPlayer.start();
					afd.close();
				} catch (Exception e) {
				}
			}
		};

		mSoundPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				TabActivityTrack.isTrackStartedFinish = false;
				if (sound.length == i) {
					mSoundPlayer.stop();
					mStop = false;
					stopAudioFocus();
					return;
				} else {
					if (timePauseID != null && timePauseID.contains(index)) {
						new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(halfSecond);
								} catch (InterruptedException e) {

								}
								startPlay(ctx, mSoundPlayer, sound, i);
							}
						}).start();

					} else {
						startPlay(ctx, mSoundPlayer, sound, i);
					}
				}
			}
		});
		t.start();
	}

	private static void startPlay(final Activity activity,
			final MediaPlayer mSoundPlayer, final int sound, int index,
			final boolean isCountDelay) {

		Thread t = new Thread() {
			public void run() {
				AssetFileDescriptor afd = activity.getResources()
						.openRawResourceFd(sound);
				try {
					mSoundPlayer.reset();
					mSoundPlayer.setDataSource(afd.getFileDescriptor(),
							afd.getStartOffset(), afd.getDeclaredLength());
					mSoundPlayer.prepare();
					mSoundPlayer.start();
					afd.close();
				} catch (Exception e) {
				}
			}
		};

		mSoundPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				TabActivityTrack.isTrackStartedFinish = false;
				stopAudioFocus();
				if (isCountDelay) {
					((TabActivityTrack) activity)
							.startCountDownTimer(activity
									.getSharedPreferences(
											Constants.PREFERENCES,
											Context.MODE_PRIVATE)
									.getInt(com.athlete.Constants.INTENT_KEY.COUNTDOUNT,
											0));
				}

			}
		});
		t.start();

	}
}
