package com.athlete.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.auth.ActivityAuthPagers;
import com.athlete.bl.BaseBl;
import com.athlete.bl.FeedBL;
import com.athlete.bl.FriendsBL;
import com.athlete.bl.IdleWorkoutBL;
import com.athlete.bl.StatsBL;
import com.athlete.bl.User2FeedBl;
import com.athlete.bl.UserBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.control.RoundedImageView;
import com.athlete.db.DatabaseHelper;
import com.athlete.services.AsyncTaskManager;
import com.athlete.util.AudioFocusHelper;
import com.athlete.util.CommonHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	/**
	 * @author edBaev
	 */
	protected UserBL userBL;
	protected ImageLoader imageLoader;
	protected FeedBL feedBL;
	protected WorkoutBL workoutBL;
	protected IdleWorkoutBL idleWorkoutBL;
	protected BaseBl baseBl;
	protected FriendsBL friendsBL;
	protected User2FeedBl user2FeedBl;
	protected StatsBL statsBL;
	protected DisplayImageOptions options;

	protected int corner5DP, corner2DP, size50dp, size40dp, size75dp,
			size100dp, padding2DP, size35dp, size105dp;
	protected SharedPreferences sp;
	protected String mUserName, mApiKey, mId;
	protected Animation anim;
	protected boolean isMetric;
	protected ImageButton mBtnRefresh, mBtnBack;
	private AsyncTaskManager taskManager = null;
	protected ImageView splash;

	protected AudioFocusHelper mAudioFocusHelper;

	protected static enum TransitionType {
		Zoom, SlideLeft, Diagonal
	}

	protected static TransitionType transitionType;
	protected long mNTPTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		startTransferAnim();

		userBL = new UserBL(getHelper());
		feedBL = new FeedBL(getHelper());
		workoutBL = new WorkoutBL(getHelper());
		friendsBL = new FriendsBL(getHelper());
		user2FeedBl = new User2FeedBl(getHelper());
		statsBL = new StatsBL(getHelper());
		baseBl = new BaseBl(this);
		imageLoader = ((AthleteApplication) getApplication()).getImageLoader();
		options = ((AthleteApplication) getApplication())
				.getDisplayImageOptions();
		corner5DP = CommonHelper.getPX(5, this);
		corner2DP = CommonHelper.getPX(2, this);
		padding2DP = CommonHelper.getPX(2, this);
		size50dp = CommonHelper.getPX(50, this);
		size75dp = CommonHelper.getPX(75, this);
		size100dp = CommonHelper.getPX(100, this);
		size35dp = CommonHelper.getPX(35, this);
		size105dp = CommonHelper.getPX(105, this);
		size40dp = CommonHelper.getPX(40, this);
		anim = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(Constants.ANIM_DURATION);
		taskManager = ((AthleteApplication) getApplication())
				.getTaskManager(getMainContext());

	}

	protected void startTransferAnim() {
		transitionType = TransitionType.Zoom;
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
	}

	public AsyncTaskManager getTaskManager() {
		return taskManager;
	}

	protected void deleteAllDatabseTable() {
		SQLiteDatabase database = getHelper().getWritableDatabase();

		database.delete(Constants.TABLE_NAME.FEED2TYPE2USER, null, null);
		database.delete(Constants.TABLE_NAME.COMMENT, null, null);
		database.delete(Constants.TABLE_NAME.FEED, null, null);
		database.delete(Constants.TABLE_NAME.POSTPICTURE, null, null);
		database.delete(Constants.TABLE_NAME.IDLE_WORKOUT, null, null);
		database.delete(Constants.TABLE_NAME.PROFILE_USER, null, null);
		database.delete(Constants.TABLE_NAME.PREFERENCE_USER, null, null);
		database.delete(Constants.TABLE_NAME.STATS, null, null);
		database.delete(Constants.TABLE_NAME.WORKOUT, null, null);
		database.delete(Constants.TABLE_NAME.USER, null, null);
		database.delete(Constants.TABLE_NAME.FRIEND_M2M, null, null);
		database.delete(Constants.TABLE_NAME.USER_M2M_FEED, null, null);
		database.delete(Constants.TABLE_NAME.CONVERSATION_M2M_USER, null, null);
		database.delete(Constants.TABLE_NAME.WORKOUT_M2M_TRACK, null, null);
		database.delete(Constants.TABLE_NAME.CONVERSATION, null, null);
		database.delete(Constants.TABLE_NAME.MESSAGE, null, null);
		database.delete(Constants.TABLE_NAME.FB_USER, null, null);
		database.delete(Constants.TABLE_NAME.FB_USER_M2M, null, null);
	}

	protected Context getMainContext() {
		Context context;
		if (getParent() != null) {
			context = getParent();
		} else {
			context = this;
		}
		return context;
	}

	protected void setVisibleInvisible(String text, TextView txtView) {
		if (text.length() > 0) {
			txtView.setText(text);
			txtView.setVisibility(View.VISIBLE);
		} else {
			txtView.setVisibility(View.GONE);
		}
	}

	protected String getUserID() {
		return getShared().getString(
				Constants.SharedPreferencesKeys.CURRENT_ID, null);
	}

	protected String getUserName() {
		return getShared().getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
	}

	protected String getApikey() {
		return getShared().getString(Constants.SharedPreferencesKeys.API_KEY,
				null);
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

	protected boolean isMetric() {
		if (sp == null) {
			sp = getShared();
		}
		if (sp.getString(Constants.INTENT_KEY.METRIC,
				getResources().getString(R.string.btn_miles)).equals(
				getResources().getString(R.string.btn_miles))) {
			isMetric = false;
		} else {
			isMetric = true;
		}
		return isMetric;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		startTransferAnim();
	}

	protected void hideKeyboard(Activity ctx) {
		try {

			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(ctx.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
		}
	}

	protected SharedPreferences getShared() {
		SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE);
		return sp;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		taskManager.finishAll();
		if (mAudioFocusHelper != null) {
			mAudioFocusHelper.abandonFocus();
		}
	}

	protected void logout(Context ctx) {
		sp = getShared();
		sp.edit().remove(Constants.SharedPreferencesKeys.USER_NAME).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.API_KEY).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.API_KEY).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.CURRENT_ID).commit();
		sp.edit().remove(Constants.INTENT_KEY.METRIC).commit();
		sp.edit().remove(Constants.INTENT_KEY.AUDIO_TIMING_METRIC).commit();
		sp.edit().remove(Constants.INTENT_KEY.AUDIO_TIMING_MILE).commit();
		sp.edit().remove(Constants.INTENT_KEY.COUNTDOUNT).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.FB_ACCES).commit();
		sp.edit().remove(getString(R.string.notify_msg)).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.SERVER).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.URI).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.FIRST_TIME).commit();
		startActivity(new Intent(ctx, ActivityAuthPagers.class));
		finish();
		transitionType = TransitionType.Zoom;
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
	}

	public class ViewHolder {
		public TextView txtName;
		public TextView txtTime;
		public TextView txtBody;
		public RoundedImageView imAva;
		public RoundedImageView imageMap;
		public TextView imType;
		public TextView txtCommentCount;
		public TextView txtLikeCount;
		public TextView txtDistance;
		public TextView txtDuration;
		public TextView txtTitle;
		public TextView txtArea;
		public TextView txtAVGPace;
		public TextView txtClimb;
		public TextView txtConnect;
		public TextView txtToSync;
		public ImageView imRecycle;
		public ProgressBar progressSplit;
		public LinearLayout itemList;
		public LinearLayout linearConnectToSync;
		public Button btnAccept;
		public ImageButton btnCancel;
	}
}
