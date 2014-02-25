package com.athlete.activity.feed;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.Feed;
import com.athlete.model.Feed2Type2User;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.get.GetFeedTask;
import com.athlete.util.AnalyticsUtils;

public class ActivityFeedFriends extends BaseActivityFeed {
	/**
	 * @author edBaev
	 */
	private final int typeFriends = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_feed);
		sp = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);

		setView();
		PreferencesUtils.setBoolean(ActivityFeedFriends.this,
				R.string.is_delete_friend, false);
		updateFeedsFromLocalDB();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityFeedFriends.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FEED_FRIENDS);
		mBtnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				updateTheFeed(null, true);
			}
		});
		if (PreferencesUtils.getBoolean(ActivityFeedFriends.this,
				R.string.is_delete_friend, false)) {
			PreferencesUtils.setBoolean(ActivityFeedFriends.this,
					R.string.is_delete_friend, false);
			updateFeedsFromLocalDB();
		}

	}

	private void updateFeedsFromLocalDB() {

		List<Feed2Type2User> feed2Type = baseBl.getListFromDBBy2Field(
				Feed2Type2User.class, Feed2Type2User.TYPE, typeFriends,
				Feed2Type2User.CURR_USER, mId);
		if (feed2Type != null && !feed2Type.isEmpty()) {
			listOfFeed.clear();
			for (int i = 0; i < feed2Type.size(); i++) {
				listOfFeed.add(feed2Type.get(i).getFeed());
			}
			Parcelable state = listView.onSaveInstanceState();
			setAdapter();
			listView.onRestoreInstanceState(state);
		} else {
			listView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void updateFeed(String id, boolean update) {
		super.updateFeed(id, update);
		updateTheFeed(id, update);
	}

	private void updateTheFeed(String later_than, final boolean update) {
		progressBar.setVisibility(View.VISIBLE);
		mBtnRefresh.setVisibility(View.GONE);
		splash.startAnimation(anim);

		OnTskCpltListener getFeed = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<List<Feed>> result;
				mBtnRefresh.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				try {
					result = (TaskResult<List<Feed>>) task.get();

					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (update) {
							listOfFeed = new LinkedList<Feed>();
						}
						if (adapter != null) {
							adapter.clear();
						}

					} else {
						if (result.getResult().size() > 0) {

							// TODO:
							if (result.getTypeFeed() == 2) {
								// go to local screen
								((TabActivityFeed) getParent()).setTab(1, true);
								// go to featured screen
							} else if (result.getTypeFeed() == 3) {
								((TabActivityFeed) getParent()).setTab(2, true);
							} else {
								if (update) {
									listOfFeed.clear();
								}

								listOfFeed.addAll(result.getResult());

								if (result.getResult().size() == 20) {
									isAllDialogsLoaded = false;
								} else {
									isAllDialogsLoaded = true;
								}
								Parcelable state = listView
										.onSaveInstanceState();
								setAdapter();
								listView.onRestoreInstanceState(state);
							}
						}

					}
				} catch (Exception e) {
				}
			}
		};
		int fallback = 0;
		if (sp.getBoolean(Constants.SharedPreferencesKeys.FIRST_TIME, true)) {
			fallback = 1;
		}
		GetFeedTask feedTask = new GetFeedTask(ActivityFeedFriends.this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, Constants.TYPE_FEED.FRIENDS, mId, null, later_than,
				null, Integer.valueOf(mId), typeFriends, update, fallback);
		getTaskManager().executeTask(feedTask, getFeed, null, true);
	}
}
