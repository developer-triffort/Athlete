package com.athlete.activity.feed;

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

public class ActivityFeedFeatured extends BaseActivityFeed {
	/**
	 * @author edBaev
	 */
	private final int typeFeatured = 3;

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
		updateFeedsFromLocalDB();
	}

	@Override
	protected void onResume() {
		super.onResume();

		AnalyticsUtils.sendPageViews(ActivityFeedFeatured.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FEED_FEATURE);

		mBtnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				updateTheFeed(null, true);
			}
		});

		if (PreferencesUtils.getBoolean(ActivityFeedFeatured.this,
				R.string.is_delete_featured, false)) {
			PreferencesUtils.setBoolean(ActivityFeedFeatured.this,
					R.string.is_delete_featured, false);
			updateFeedsFromLocalDB();
		}

	}

	private void updateFeedsFromLocalDB() {
		List<Feed2Type2User> feed2Type = baseBl.getListFromDBBy2Field(
				Feed2Type2User.class, Feed2Type2User.TYPE, typeFeatured,
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
				progressBar.setVisibility(View.GONE);
				mBtnRefresh.setVisibility(View.VISIBLE);
				TaskResult<List<Feed>> result;
				try {
					result = (TaskResult<List<Feed>>) task.get();

					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					} else {
						if (result.getResult().size() > 0) {
							if (update) {
								listOfFeed.clear();
							}

							listOfFeed.addAll(result.getResult());
							if (result.getResult().size() == 20) {
								isAllDialogsLoaded = false;
							} else {
								isAllDialogsLoaded = true;
							}
							Parcelable state = listView.onSaveInstanceState();
							setAdapter();
							listView.onRestoreInstanceState(state);

						}
					}
				} catch (Exception e) {
				}
			}
		};

		GetFeedTask feedTask = new GetFeedTask(ActivityFeedFeatured.this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, Constants.TYPE_FEED.FEATURED, null, null, later_than,
				null, Integer.valueOf(mId), typeFeatured, update, 0);
		getTaskManager().executeTask(feedTask, getFeed, null, true);
	}
}
