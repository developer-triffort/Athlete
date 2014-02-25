package com.athlete.activity.track.details;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.TabActivityMain;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.bl.FeedBL;
import com.athlete.bl.UserBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils.LocationIterator;
import com.athlete.google.android.apps.mytracks.services.TrackRecordingService;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;
import com.athlete.model.Feed;
import com.athlete.model.Feed2Type2User;
import com.athlete.model.Split;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.model.WorkOut;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.delete.DeleteFeedTask;
import com.athlete.util.AnalyticsUtils;

public class ActivitySplitsDetails extends BaseActivity {
	private ListView listSplit;
	private BaseListAdapter<Split> adapter;
	private double maxAvg;
	private String type;
	private MyTracksProviderUtils myTracksProviderUtils;
	private double totalDistance;
	private long totalTime, prevMaxMovingTime;
	private long trackID;
	private Split mSplit;
	private List<Split> mSplits;
	private double firstElevation, prevMaxTotalDistance;
	private double ONE_KM = 1;
	private double metersInKm = 1000.0;
	private double minute = 60.0, hundred = 100d;
	private TextView mTxtTitle;
	private User user;
	private WorkOut workout;
	private Feed feed;
	private RelativeLayout progressBar;
	private ImageView splash;
	private Animation animLarge;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.actv_split_details);
		trackID = getIntent().getLongExtra(Constants.INTENT_KEY.ID, -1L);
		String userId = getIntent().getStringExtra(
				Constants.INTENT_KEY.USER_DETAILS);
		int workOutId = getIntent().getIntExtra(
				Constants.INTENT_KEY.WORKOUT_ID, -1);
		workoutBL = new WorkoutBL(getHelper());
		feedBL = new FeedBL(getHelper());
		workout = workoutBL.getBy(String.valueOf(workOutId));
		feed = workout.getFeed();
		if (feed == null) {
			feed = feedBL.getBy(workout.getPost());
		}
		userBL = new UserBL(getHelper());
		user = userBL.getBy(userId);
		if (PreferencesUtils.getMetricUnit(ActivitySplitsDetails.this)) {
			ONE_KM *= metersInKm;
		} else {
			ONE_KM *= UnitConversions.MI_TO_M;
		}
		setView();
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
		findViewById(R.id.btnDelete).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						alertDialog();

					}
				});
		progressBar = (RelativeLayout) findViewById(R.id.progressBar);
		splash = (ImageView) findViewById(R.id.imVLoading);
		// Start animating the image
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
	}

	private void alertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_discard))
				.setMessage(getString(R.string.message_remove_workout))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								deleteWorkout();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySplitsDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SPLIT_DETAILS);
	}

	public void updateSplits() {
		for (int i = 0; i < mSplits.size(); i++) {
			if (mSplits.size() > i + 1) {
				maxAvg = Math.max(maxAvg, Math.max(mSplits.get(i).getAvgPace(),
						mSplits.get(i + 1).getAvgPace()));
			}
		}
		if (mSplits != null && !mSplits.isEmpty()) {
			setAdapter();
		}
	}

	private void setView() {
		myTracksProviderUtils = MyTracksProviderUtils.Factory.get(this);
//		locationFactory = new DoubleBufferedLocationFactory();
		mSplit = new Split();
		mSplits = new LinkedList<Split>();
		mSplits.add(mSplit);
		type = isMetric() ? getString(R.string.split_kilometer)
				: getString(R.string.split_mile);
		type += " ";
		listSplit = (ListView) findViewById(R.id.listSplit);
		getLocationArr();
		mTxtTitle = (TextView) findViewById(R.id.txtTitle);
		setTitleTxt();
	}

	private void setTitleTxt() {
		if (user != null) {
			mTxtTitle.setText(user.getFirstName() + " " + user.getLastName());
		}
	}

	private void shoProgress() {
		progressBar.setVisibility(View.VISIBLE);
		findViewById(R.id.btnDelete).setVisibility(View.GONE);
		splash.startAnimation(animLarge);
	}

	private void hideProgress() {
		findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
		splash.clearAnimation();
		progressBar.setVisibility(View.GONE);
	}

	private void deleteWorkout() {
		shoProgress();
		OnTskCpltListener deleteWorkout = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.getResult()) {
						workoutBL.delete(workout, WorkOut.class);
						feedBL.delete(feed, Feed.class);

						PreferencesUtils.setBoolean(ActivitySplitsDetails.this,
								R.string.is_delete_featured, true);

						PreferencesUtils.setBoolean(ActivitySplitsDetails.this,
								R.string.is_delete_friend, true);

						PreferencesUtils.setBoolean(ActivitySplitsDetails.this,
								R.string.is_delete_local, true);
						userBL.deleteByField(Feed2Type2User.FEED,
								Feed2Type2User.class, feed);

						finish();
						startActivity(new Intent(ActivitySplitsDetails.this,
								TabActivityMain.class)
								.putExtra(Constants.INTENT_KEY.BOOLEAN_VALUE,
										true)
								.setFlags(
										Intent.FLAG_ACTIVITY_CLEAR_TOP
												| Intent.FLAG_ACTIVITY_SINGLE_TOP));
					}
				} catch (Exception e) {
				}
			}
		};
		DeleteFeedTask deleteWorkoutTask = new DeleteFeedTask(
				ActivitySplitsDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), feed.getId());
		getTaskManager().executeTask(deleteWorkoutTask, deleteWorkout, null,
				true);
	}

	private void newSplits(double currentElevation, double totalDistance,
			long time) {
		mSplit.setClimb(currentElevation - firstElevation);
		firstElevation = currentElevation;
		mSplit = new Split();
		prevMaxMovingTime = time;
		prevMaxTotalDistance = totalDistance;
		mSplits.add(0, mSplit);
	}

	private void setSplit(double currentElevation, double totalDistance,
			long time) {
		mSplit.setClimb(currentElevation - firstElevation);
		double distance = totalDistance - prevMaxTotalDistance;
		long movingTime = time - prevMaxMovingTime;
		double speed = distance / ((double) movingTime / metersInKm);
		speed *= UnitConversions.MS_TO_KMH;
		if (!isMetric) {
			speed *= UnitConversions.KM_TO_MI;
		}
		double pace = speed == 0 ? 0.0 : minute / speed;
		pace = (int) pace + ((int) ((pace - (int) pace) * minute)) / hundred;
		mSplit.setAvgPace(pace);
	}

	private void getLocationArr() {
		LocationIterator iterator = myTracksProviderUtils
				.getTrackPointLocationIterator(trackID, 1, false,
						MyTracksProviderUtils.DEFAULT_LOCATION_FACTORY);
		Location locPrev = null;
		while (iterator.hasNext()) {
			Location location = new Location(iterator.next());
			if (location.getLatitude() == TrackRecordingService.PAUSE_LATITUDE) {
				location = new Location(iterator.next());
				location = new Location(iterator.next());
				locPrev = null;
			}
			if (locPrev != null) {
				double distance = location.distanceTo(locPrev);
				long time = (location.getTime() - locPrev.getTime());
				totalDistance += distance;
				totalTime += time;
			} else {
				firstElevation = location.getAltitude();
			}
			if (totalDistance >= ONE_KM * mSplits.size()) {
				newSplits(location.getAltitude(), totalDistance, totalTime);
			}
			setSplit(location.getAltitude(), totalDistance, totalTime);
			locPrev = location;
		}
		iterator.close();
		if (mSplits != null && !mSplits.isEmpty()) {
			updateSplits();
		}
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<Split>(this, mSplits, R.layout.item_split) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final Split item = getItem(position);
				if (convertView == null) {
					holder = new ViewHolder();
					convertView = getLayout();
					holder.progressSplit = (ProgressBar) convertView
							.findViewById(R.id.progressBarAVGPace);
					holder.txtClimb = (TextView) convertView
							.findViewById(R.id.txtClimb);
					holder.txtAVGPace = (TextView) convertView
							.findViewById(R.id.txtAvgPace);
					convertView.setTag(holder);
				} else {
					if (convertView.getTag() != null
							&& convertView.getTag() instanceof ViewHolder) {
						holder = (ViewHolder) convertView.getTag();
					} else {
						return getLayout();
					}
				}
				if (maxAvg != 0) {
					holder.progressSplit.setMax((int) (maxAvg * hundred));
				} else {
					holder.progressSplit
							.setMax((int) (item.getAvgPace() * hundred));
				}
				holder.progressSplit
						.setProgress((int) (item.getAvgPace() * hundred));
				holder.txtAVGPace.setText(type
						+ (mSplits.size() - position)
						+ " - "
						+ String.valueOf(
								new DecimalFormat(Constants.FORMATS.TWO_ZERO)
										.format(item.getAvgPace()))
								.replace(",", "'").replace(".", "'"));
				holder.txtClimb.setText(String.valueOf((int) (Math.abs(item
						.getClimb() * UnitConversions.M_TO_FT)))
						+ " " + getString(R.string.ft));
				if (item.getClimb() >= 0) {
					holder.txtClimb.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.splits_arrow_up), null, null,
							null);
				} else {
					holder.txtClimb.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.splits_arrow_down), null, null,
							null);
				}
				return convertView;
			}
		};
		listSplit.setAdapter(adapter);
	}
}
