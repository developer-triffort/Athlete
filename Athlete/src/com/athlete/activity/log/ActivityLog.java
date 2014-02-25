package com.athlete.activity.log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.track.details.BaseTrackDetails;
import com.athlete.adapter.SectionWorkoutAdapter;
import com.athlete.bl.FeedBL;
import com.athlete.bl.IdleWorkoutBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.control.RoundedImageView;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.model.IdleWorkOut;
import com.athlete.model.TaskResult;
import com.athlete.model.WorkOut;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SendPicturePostTask;
import com.athlete.services.task.SendWorkoutFileTask;
import com.athlete.services.task.SendWorkoutJSONTask;
import com.athlete.services.task.get.GetWorkoutByIDTask;
import com.athlete.services.task.get.GetWorkoutTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;
import com.segment.SegmentListView;

public class ActivityLog extends BaseActivity {
	private SegmentListView lsComposer;
	private SectionWorkoutAdapter adapter;
	private List<WorkOut> listOfWorkOut;
	private List<IdleWorkOut> listOfIdleWorkOut;
	private HashMap<Integer, IdleWorkOut> hashIdleWorkOut;
	private boolean isAllDialogsLoaded;
	private String sizeOfMap = "&size=";
	private ArrayList<String> arrayListID;
	private ArrayList<String> arrayListDownloadID;
	private ArrayList<String> arrayListGPXID;
	private Animation animLargeConnectToSync;
	private Animation animLarge;
	private JSONObject jsonObjSend;
	private boolean isUploading;
	private View emptyview;
	private RelativeLayout progressBar;
	private ImageView splash;
	private ImageButton btnRefresh;
	private Animation anim;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);

		anim = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(Constants.ANIM_DURATION);
		sp = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);
		int sizeOfMapInPX = CommonHelper.getPX(75, this);
		sizeOfMap = sizeOfMap + sizeOfMapInPX + "x" + sizeOfMapInPX;
		animLargeConnectToSync = AnimationUtils.loadAnimation(this,
				R.anim.anim_connect_to_sync);
		animLargeConnectToSync.setInterpolator(new LinearInterpolator());
		animLargeConnectToSync.setRepeatCount(Animation.INFINITE);
		animLargeConnectToSync.setDuration(Constants.ANIM_DURATION);

		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
		emptyview = new View(ActivityLog.this);
		splash = (ImageView) findViewById(R.id.imVLoading);
		btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
		progressBar = (RelativeLayout) findViewById(R.id.relativeCircle);
		emptyview.setBackgroundResource(R.drawable.background);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startProgress();
				addLog(0);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityLog.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.LOG);
		startProgress();
		setView();
		isAllDialogsLoaded = false;
		updateWorkout();
		if (listOfWorkOut != null && !listOfWorkOut.isEmpty()
				&& listOfIdleWorkOut != null && !listOfIdleWorkOut.isEmpty()
				&& !isUploading) {
			startUpload(listOfWorkOut.get(listOfIdleWorkOut.size() - 1));
		}
	}

	private void startProgress() {
		progressBar.setVisibility(View.VISIBLE);
		btnRefresh.setVisibility(View.INVISIBLE);
		splash.startAnimation(anim);
	}

	private void stopProgress() {
		btnRefresh.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.INVISIBLE);
		splash.clearAnimation();
	}

	@SuppressLint("UseSparseArrays")
	protected void setView() {
		feedBL = new FeedBL(getHelper());
		workoutBL = new WorkoutBL(getHelper());
		idleWorkoutBL = new IdleWorkoutBL(getHelper());
		listOfWorkOut = new LinkedList<WorkOut>();
		listOfIdleWorkOut = new LinkedList<IdleWorkOut>();
		hashIdleWorkOut = new HashMap<Integer, IdleWorkOut>();
		lsComposer = (SegmentListView) findViewById(R.id.lsComposer);
		lsComposer.setEmptyView(emptyview);
	}

	private void updateWorkout() {
		arrayListID = new ArrayList<String>();
		arrayListDownloadID = new ArrayList<String>();
		arrayListGPXID = new ArrayList<String>();
		listOfWorkOut = workoutBL.getListFromDBByField(WorkOut.class,
				WorkOut.USER, mId);
		listOfIdleWorkOut = idleWorkoutBL.getListFromDBByField(
				IdleWorkOut.class, IdleWorkOut.USER, mId);
		if (listOfIdleWorkOut != null && !listOfIdleWorkOut.isEmpty()) {
			convertIdletoWorkout();
		}
		stopProgress();
		if (listOfWorkOut != null && !listOfWorkOut.isEmpty()) {
			setAdapter();
		} else {
			lsComposer.setVisibility(View.GONE);
		}

	}

	private void convertIdletoWorkout() {
		for (IdleWorkOut idleWorkOut : listOfIdleWorkOut) {
			hashIdleWorkOut.put(idleWorkOut.getId(), idleWorkOut);
			WorkOut workOut = new WorkOut();
			arrayListID.add(String.valueOf(idleWorkOut.getId()));
			if (idleWorkOut.isNowDownload()) {
				arrayListDownloadID.add(String.valueOf(idleWorkOut.getId()));
			}

			if (idleWorkOut.getTrackPath() != null) {
				arrayListGPXID.add(String.valueOf(idleWorkOut.getId()));
			}
			workOut.setDuration(idleWorkOut.getDuration());
			workOut.setId(idleWorkOut.getId());
			workOut.setPost(idleWorkOut.getPost());
			workOut.setDistance(idleWorkOut.getDistance());

            workOut.setActivityType(idleWorkOut.getActivityType());
            workOut.setActivitySubType(idleWorkOut.getActivitySubType());

            workOut.setCalories(idleWorkOut.getCalories());
			workOut.setIdUser(mId);
			workOut.setRunDate(CommonHelper
					.getDateFormatYYYYMMDDtHHMMSS(((AthleteApplication) getApplication())
							.getDate()));
			workOut.setTitle(idleWorkOut.getTitle());
			workOut.setPostBody(idleWorkOut.getPostBody());
			listOfWorkOut.add(0, workOut);
		}

	}

	protected void setAdapter() {
		if (listOfWorkOut.size() >= 20) {
			isAllDialogsLoaded = true;
		}
		lsComposer.setVisibility(View.VISIBLE);
		adapter = new SectionWorkoutAdapter(ActivityLog.this, listOfWorkOut) {
			@Override
			public int getCount() {
				return super.getCount() + (!isAllDialogsLoaded ? 1 : 0);
			}

			@SuppressWarnings("deprecation")
			@Override
			public View getSectionView(int position, View convertView,
					ViewGroup parent) {
				ViewHolder holder;
				if (position == getCount() - 1 && !isAllDialogsLoaded) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.row_update_footer,
							null);
					view.setId(R.layout.row_update_footer);
					View splash = view.findViewById(R.id.imVLoading);
					splash.startAnimation(animLarge);
					startProgress();
					addLog(getCount() - 1);
					return view;
				}
				if (convertView == null
						|| convertView.getId() != R.layout.item_log) {
					holder = new ViewHolder();
					convertView = LayoutInflater.from(ActivityLog.this)
							.inflate(R.layout.item_log, null);
					convertView.setId(R.layout.item_log);
					holder.imageMap = (RoundedImageView) convertView
							.findViewById(R.id.imageMap);
					holder.linearConnectToSync = (LinearLayout) convertView
							.findViewById(R.id.linearConnectToSync);
					holder.txtTitle = (TextView) convertView
							.findViewById(R.id.txtTitle);
					holder.txtConnect = (TextView) convertView
							.findViewById(R.id.txtConnect);
					holder.txtToSync = (TextView) convertView
							.findViewById(R.id.txtToSync);
					holder.imRecycle = (ImageView) convertView
							.findViewById(R.id.imRecycle);
					holder.txtTime = (TextView) convertView
							.findViewById(R.id.txtTime);

					holder.imType = (TextView) convertView
							.findViewById(R.id.txtActivityType);

                    Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/athlete-regular-webfont.ttf");
                    holder.imType.setTypeface(typeFace);

					holder.txtDistance = (TextView) convertView
							.findViewById(R.id.txtDistantionTime);
					holder.txtDuration = (TextView) convertView
							.findViewById(R.id.txtMeanValue);
					holder.imageMap.setCornerRadius(corner2DP, size75dp);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				final WorkOut workOut = getItem(position);

				setVisibleInvisible(workOut.getTitle(), holder.txtTitle);

				holder.txtTime.setText(CommonHelper.getLastSeen(CommonHelper
						.getLongYYYYMMDDtHHMMSS(workOut.getRunDate()),
						ActivityLog.this));
				holder.txtDistance.setText(CommonHelper.convertMetersToMiles(
						workOut.getDistance(), isMetric())
						+ CommonHelper.getMiOrKm(isMetric())
						+ CommonHelper.getDateMSS(workOut.getDuration(),
								ActivityLog.this));
				if (workOut.getDuration() != 0) {
					holder.txtDuration.setText("("
							+ StringUtils.formatSpeed(
									ActivityLog.this,
									workOut.getDistance()
											/ workOut.getDuration(), isMetric,
									false) + ")");
				}
				CommonHelper.setType(holder.imType, workOut, null, getResources());
				if (arrayListID.contains(String.valueOf(workOut.getId()))) {
					holder.imageMap.setVisibility(View.GONE);
					holder.linearConnectToSync.setVisibility(View.VISIBLE);
					if (arrayListDownloadID.contains(String.valueOf(workOut
							.getId()))) {
						holder.txtConnect.setVisibility(View.INVISIBLE);
						holder.txtToSync.setVisibility(View.INVISIBLE);
						holder.imRecycle.startAnimation(animLargeConnectToSync);

					} else {
						holder.txtConnect.setVisibility(View.VISIBLE);
						holder.txtToSync.setVisibility(View.VISIBLE);
						holder.imRecycle.clearAnimation();
					}
					holder.linearConnectToSync
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (!arrayListDownloadID.contains(String
											.valueOf(workOut.getId()))) {

										startUpload(workOut);
									}
									adapter.notifyDataSetChanged();
								}
							});
				} else {
					holder.imageMap.setVisibility(View.VISIBLE);
					holder.linearConnectToSync.setVisibility(View.GONE);
					if (workOut.getStaticMapUrl() != null
							&& workOut.getStaticMapUrl().length() > 0) {
						imageLoader.displayImage(workOut.getStaticMapUrl()
								+ sizeOfMap, holder.imageMap, options);
						holder.imageMap.setBackgroundDrawable(null);
					} else {
						holder.imageMap.setImageDrawable(getResources()
								.getDrawable(R.drawable.manual_run_icon));
					}
				}
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!arrayListID.contains(String.valueOf(workOut
								.getId()))) {
							startActivity(new Intent(ActivityLog.this,
									BaseTrackDetails.class).putExtra(
									Constants.INTENT_KEY.ID, workOut.getId()));
						}
					}
				});
				return convertView;
			}
		};
		lsComposer.setPinnedHeaderView(LayoutInflater.from(this).inflate(
				R.layout.item_composer_header_log, lsComposer, false));

		lsComposer.setAdapter(adapter);
	}

	private void startUpload(WorkOut workOut) {
		isUploading = true;
		arrayListDownloadID.add(String.valueOf(workOut.getId()));
		if (hashIdleWorkOut.get(workOut.getId()) != null) {
			if (hashIdleWorkOut.get(workOut.getId()).isOnlyPhoto()) {
				// upload photo
				Log.d("upload photo", "upload photo");
				if (workOut.getPost() != null) {
					ArrayList<String> arrayList = new ArrayList<String>();
					for (String str : hashIdleWorkOut.get(workOut.getId())
							.getPhotoPath()) {
						arrayList.add(str);
					}
					uploadPhoto(arrayList, String.valueOf(workOut.getPost()),
							hashIdleWorkOut.get(workOut.getId()).getId(),
							workOut);
				}
			} else {
				// upload track
				if (!hashIdleWorkOut.get(workOut.getId()).isOnlyPhoto()) {
					hashIdleWorkOut.get(workOut.getId()).setNowDownload(true);
					baseBl.createOrUpdate(IdleWorkOut.class,
							hashIdleWorkOut.get(workOut.getId()));
					if (!arrayListGPXID
							.contains(String.valueOf(workOut.getId()))) {
						// upload from manual
						createJSON(workOut);
					} else {
						// upload GPX file
						uploadWorkout(hashIdleWorkOut.get(workOut.getId())
								.getTrackPath(), workOut);
					}
				}
			}
		}
	}

	private void createJSON(WorkOut workOut) {
		IdleWorkOut idleWorkOut = hashIdleWorkOut.get(workOut.getId());
		jsonObjSend = new JSONObject();
		putJSON(WorkOut.DISTANCE, idleWorkOut.getDistance());
		putJSON(WorkOut.DURATION, idleWorkOut.getDuration());
		if (idleWorkOut.getTitle().length() > 0) {
			putJSON(WorkOut.TITLE, idleWorkOut.getTitle());
		}
		putJSON(WorkOut.POST_BODY, idleWorkOut.getPostBody());
        putJSON(WorkOut.ACTIVITY_TYPE, idleWorkOut.getActivityType());
		putJSON(WorkOut.ACTIVITY_SUBTYPE, idleWorkOut.getActivitySubType());
		putJSON(WorkOut.RUN_DATE, workOut.getRunDate());
		putJSON(IdleWorkOut.PRIVACY, idleWorkOut.getPrivacy().toLowerCase());
		putJSON(IdleWorkOut.FB_ACCES, idleWorkOut.getFbAcces());
		postWorkoutJSON(jsonObjSend, workOut);
	}

	private void putJSON(String name, Object value) {
		try {
			jsonObjSend.put(name, value);
		} catch (JSONException e) {
		}
	}

	private void postWorkoutJSON(JSONObject jsonObject, final WorkOut workOut) {
		final int id = workOut.getId();
		OnTskCpltListener endWorkoutJSON = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<String> result;
				try {
					result = (TaskResult<String>) task.get();
					checkResult(result, id);
				} catch (Exception e) {
				}
			}
		};
		SendWorkoutJSONTask sendWorkoutJSONTask = new SendWorkoutJSONTask(
				ActivityLog.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), jsonObject);
		getTaskManager().executeTask(sendWorkoutJSONTask, endWorkoutJSON, null,
				true);
	}

	private void uploadWorkout(String trackPath, final WorkOut workOut) {
		final int id = workOut.getId();
		OnTskCpltListener upload = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<String> result;
				try {
					result = (TaskResult<String>) task.get();
					checkResult(result, id);
				} catch (Exception e) {

				}
			}
		};
		SendWorkoutFileTask sendWorkoutTask = new SendWorkoutFileTask(
				ActivityLog.this, new File(trackPath), getURLHost(),
				getPublicKey(), getPrivateKey(), mApiKey, mUserName);
		getTaskManager().executeTask(sendWorkoutTask, upload, null, true);
	}

	private void getWorkoutByID(final TaskResult<String> resultString,
			final int id) {
		OnTskCpltListener getWorkoutByID = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<WorkOut> result;
				try {
					result = (TaskResult<WorkOut>) task.get();
					if (!result.isError() && result.getResult() != null) {
						ArrayList<String> arrayList = new ArrayList<String>();
						for (String str : hashIdleWorkOut.get(id)
								.getPhotoPath()) {
							arrayList.add(str);
						}
						if (arrayList.size() > 0) {
							hashIdleWorkOut.get(id).setOnlyPhoto(true);
							hashIdleWorkOut.get(id).setPost(
									result.getResult().getPost());
							baseBl.createOrUpdate(IdleWorkOut.class,
									hashIdleWorkOut.get(id));
							uploadPhoto(arrayList,
									result.getResult().getPost(), id,
									result.getResult());
						} else {
							baseBl.delete(hashIdleWorkOut.get(id),
									IdleWorkOut.class);
							baseBl.createOrUpdate(WorkOut.class,
									result.getResult());
							updateWorkout();
							isUploading = false;
						}
					}
				} catch (Exception e) {

				}
			}
		};

		GetWorkoutByIDTask getWorkoutTask = new GetWorkoutByIDTask(
				ActivityLog.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(),
				CommonHelper.getLastCompanionInt(resultString.getResult()),
				false);
		getTaskManager()
				.executeTask(getWorkoutTask, getWorkoutByID, null, true);
	}

	private void addLog(final int offset) {

		OnTskCpltListener getWorkout = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {

				TaskResult<List<WorkOut>> result;
				try {
					result = (TaskResult<List<WorkOut>>) task.get();

					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					} else {
						if (result.getResult().size() > 0) {
							if (result.getResult().size() == 20) {
								isAllDialogsLoaded = false;
							} else {
								isAllDialogsLoaded = true;
							}
							if (offset == 0) {
								listOfWorkOut = result.getResult();
								updateWorkout();
							} else {
								Parcelable state = lsComposer
										.onSaveInstanceState();
								listOfWorkOut.addAll(result.getResult());
								updateWorkout();
								lsComposer.onRestoreInstanceState(state);
							}
						}
					}
				} catch (Exception e) {
				}
				stopProgress();
			}
		};

		GetWorkoutTask workoutTask = new GetWorkoutTask(ActivityLog.this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, getUserID(), offset, true);
		getTaskManager().executeTask(workoutTask, getWorkout, null, true);
	}

	private void checkResult(TaskResult<String> result, int id) {
		if (!result.isError()) {
			if (result.getResult().equalsIgnoreCase(
					String.valueOf(HttpUtil.error206))) {
				baseBl.delete(hashIdleWorkOut.get(id), IdleWorkOut.class);

				updateWorkout();
				isUploading = false;
			} else {
				getWorkoutByID(result, id);
			}
		} else {
			hashIdleWorkOut.get(id).setNowDownload(false);
			baseBl.createOrUpdate(IdleWorkOut.class, hashIdleWorkOut.get(id));
			updateWorkout();
			isUploading = false;
		}

	}

	private void uploadPhoto(final ArrayList<String> pathes,
			final String postID, final int idleID, final WorkOut out) {

		if (pathes.size() > 0) {
			OnTskCpltListener upload = new OnTskCpltListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void onTaskComplete(
						@SuppressWarnings("rawtypes") BaseTask task) {
					TaskResult<Boolean> result;
					try {

						result = (TaskResult<Boolean>) task.get();
						if (!result.isError() && result.getResult()) {
							Log.d("Uploading Done",
									"Uploading Done " + pathes.size());
							pathes.remove(pathes.size() - 1);
							hashIdleWorkOut.get(idleID).setPhotoPath(
									pathes.toArray(new String[pathes.size()]));
							baseBl.createOrUpdate(IdleWorkOut.class,
									hashIdleWorkOut.get(idleID));
							if (pathes.size() > 0) {
								uploadPhoto(pathes, postID, idleID, out);
							} else {
								baseBl.delete(hashIdleWorkOut.get(idleID),
										IdleWorkOut.class);
								baseBl.createOrUpdate(WorkOut.class, out);
								updateWorkout();
								isUploading = false;
							}
						} else {
							Log.d("result.isError()", "true");
							hashIdleWorkOut.get(idleID).setPhotoPath(
									pathes.toArray(new String[pathes.size()]));
							hashIdleWorkOut.get(idleID).setNowDownload(false);
							baseBl.createOrUpdate(IdleWorkOut.class,
									hashIdleWorkOut.get(idleID));
							// baseBl.delete(hashIdleWorkOut.get(idleID),
							// IdleWorkOut.class);
							updateWorkout();
							isUploading = false;
						}
					} catch (Exception e) {

					}
				}
			};
			if (pathes.size() > 0)
				try {
					Log.d("PATH", pathes.get(pathes.size() - 1));
					File file = new File(pathes.get(pathes.size() - 1));
					SendPicturePostTask sendPhotoTask = new SendPicturePostTask(
							ActivityLog.this, null, file, getURLHost(),
							getPublicKey(), getPrivateKey(), getApikey(),
							getUserName(), postID);
					getTaskManager().executeTask(sendPhotoTask, upload, null,
							false);
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("Exception", "Uploading Exception Complete");
					baseBl.delete(hashIdleWorkOut.get(idleID),
							IdleWorkOut.class);
					updateWorkout();
					isUploading = false;
				}
		}
	}
}
