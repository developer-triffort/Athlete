package com.athlete.activity.feed;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.R.color;
import com.athlete.activity.TabActivityMain;
import com.athlete.activity.auth.ActivityAuthPagers;
import com.athlete.activity.user.ActivityFriendInvite;
import com.athlete.activity.user.ActivitySearchUsers;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.FriendsBL;
import com.athlete.db.DatabaseHelper;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.Conversation;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.get.GetConversationTask;
import com.athlete.services.task.get.GetFriendsTask;
import com.athlete.util.AnalyticsUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseTabActivity;

@SuppressWarnings("deprecation")
public class TabActivityFeed extends OrmLiteBaseTabActivity<DatabaseHelper> {
	/**
	 * @author edBaev
	 */
	private TabHost tabs;
	private SharedPreferences sp;
	private ImageButton btnFriend;
	private TextView txtCountFriends;
	private String mId, mUserName, mApiKey;
	private BaseOperationsBL baseBL;
	private FriendsBL friendsBL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_feed);
		sp = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
		baseBL = new BaseOperationsBL(getHelper());
		friendsBL = new FriendsBL(getHelper());
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		if (mId == null) {
			logout();
		} else {
			setupTabs();
		}
		findViewById(R.id.btnSearch).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AnalyticsUtils
								.sendPageViews(
										TabActivityFeed.this,
										AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FEED_SEARCH);
						startActivity(new Intent(TabActivityFeed.this,
								ActivitySearchUsers.class));

					}
				});
		findViewById(R.id.btnFriend).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(TabActivityFeed.this,
								ActivityFriendInvite.class));

					}
				});
		getConversation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getAllFriend();
	}

	private String getURLHost() {
		return ((AthleteApplication) getApplication()).getUrlHost();
	}

	private String getPublicKey() {
		return ((AthleteApplication) getApplication()).getPublicKey();
	}

	private String getPrivateKey() {
		return ((AthleteApplication) getApplication()).getPrivateKey();
	}

	private void getAllFriend() {

		OnTskCpltListener getFeed = new OnTskCpltListener() {
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {

				List<User> pending = friendsBL.getFriendByUserPending(mId);

				if (pending != null) {
					PreferencesUtils.setString(TabActivityFeed.this,
							R.string.friends, String.valueOf(pending.size()));
				}
				setFriendTitle();
			}
		};

		GetFriendsTask friendTask = new GetFriendsTask(TabActivityFeed.this,
				baseBL, getURLHost(), getPublicKey(), getPrivateKey(),
				mUserName, mApiKey, baseBL.getFromDBByField(User.class,
						User.ID, mId), "0", null);
		((AthleteApplication) getApplication()).getTaskManager(
				TabActivityFeed.this).executeTask(friendTask, getFeed, null,
				true);

	}

	private void getConversation() {

		OnTskCpltListener getConversation = new OnTskCpltListener() {
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {

				List<Conversation> conv = baseBL.getListFromDBByField(
						Conversation.class, Conversation.HAS_UNREAD_MESSAGES,
						true);
				if (conv != null) {
					PreferencesUtils.setString(TabActivityFeed.this,
							R.string.notify_msg, String.valueOf(conv.size()));
					((TabActivityMain) getParent()).setTextNotify(String
							.valueOf(conv.size()));
				}

			}
		};
		GetConversationTask conversationTask = new GetConversationTask(
				TabActivityFeed.this, baseBL, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, null, mId, null);
		((AthleteApplication) getApplication()).getTaskManager(
				TabActivityFeed.this).executeTask(conversationTask,
				getConversation, null, true);
	}

	private void setupTabs() {
		tabs = getTabHost();
		btnFriend = (ImageButton) findViewById(R.id.btnFriend);
		txtCountFriends = (TextView) findViewById(R.id.txtCountFriends);
		setFriendTitle();
		TabHost.TabSpec tab = tabs.newTabSpec("1");
		Intent intent = new Intent(this, ActivityFeedFriends.class);
		tab.setContent(intent);
		Button btnFriend = new Button(this);
		btnFriend.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.top_bar_tab_3));
		btnFriend.setText(getString(R.string.btn_friends));
		btnFriend.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		setBtnType(btnFriend);
		tab.setIndicator(btnFriend);
		tabs.addTab(tab);

		tab = tabs.newTabSpec("2");
		intent = new Intent(this, ActivityFeedLocal.class);
		tab.setContent(intent);
		Button btnLocal = new Button(this);
		btnLocal.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.top_bar_tab_2));
		btnLocal.setText(getString(R.string.btn_local));
		btnLocal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		setBtnType(btnLocal);
		tab.setIndicator(btnLocal);
		tabs.addTab(tab);

		tab = tabs.newTabSpec("3");
		intent = new Intent(this, ActivityFeedFeatured.class);
		tab.setContent(intent);
		Button btnFeatured = new Button(this);
		btnFeatured.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.top_bar_tab_1));
		btnFeatured.setText(getString(R.string.btn_featured));
		btnFeatured.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		setBtnType(btnFeatured);

		tab.setIndicator(btnFeatured);
		tabs.addTab(tab);

	}

	public void setTab(int index, boolean isAcces) {
		if (sp.getBoolean(Constants.SharedPreferencesKeys.FIRST_TIME, true)) {
			if (isAcces) {
				sp.edit().putBoolean(
						Constants.SharedPreferencesKeys.FIRST_TIME, false).commit();
			}
			tabs.setCurrentTab(index);
		}
	}

	private void setFriendTitle() {
		String strCount = PreferencesUtils.getString(TabActivityFeed.this,
				R.string.friends, "0");
		if (strCount.equals("0")) {
			txtCountFriends.setVisibility(View.GONE);
			btnFriend.setImageResource(R.drawable.icon_userplus2);
		} else {
			txtCountFriends.setVisibility(View.VISIBLE);
			txtCountFriends.setText(strCount);
			btnFriend.setImageResource(R.drawable.icon_userplus);
		}
	}

	protected void logout() {
		sp.edit().remove(Constants.SharedPreferencesKeys.USER_NAME).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.API_KEY).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.API_KEY).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.CURRENT_ID).commit();
		sp.edit().remove(Constants.INTENT_KEY.METRIC).commit();
		sp.edit().remove(Constants.INTENT_KEY.COUNTDOUNT).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.FB_ACCES).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.FIRST_TIME).commit();
		startActivity(new Intent(TabActivityFeed.this, ActivityAuthPagers.class));
		finish();
	}

	private void setBtnType(Button btn) {
		btn.setGravity(Gravity.CENTER);
		btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
		btn.setTextColor(getResources().getColorStateList(color.tab_feed));
		btn.setTypeface(null, Typeface.BOLD);
	}
}
