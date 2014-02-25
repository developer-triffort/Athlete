package com.athlete.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.feed.TabActivityFeed;
import com.athlete.activity.log.ActivityLog;
import com.athlete.activity.msg.ActivityConversation;
import com.athlete.activity.setup.ActivitySetup;
import com.athlete.activity.track.TabActivityTrack;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.FriendsBL;
import com.athlete.db.DatabaseHelper;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.Conversation;
import com.athlete.model.User;
import com.j256.ormlite.android.apptools.OrmLiteBaseTabActivity;

@SuppressWarnings("deprecation")
public class TabActivityMain extends OrmLiteBaseTabActivity<DatabaseHelper> {
	/**
	 * @author edBaev
	 */

	private String mTrackPath;
	private TabHost mTabs;
	private String FEED_TAG = "feedTag";
	private String CONVERSATION_TAG = "conversationTag";
	private String SETUP_TAG = "setupTag";
	private String LOG_TAG = "logTag";
	private String FAKE_TAG = "fakeTag";
	public TextView txtNotify;
	public String stringNotify;
	private BaseOperationsBL baseBl;
	private FriendsBL friendsBL;
	private final int logId = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tab_main);
		boolean isLog = getIntent().getBooleanExtra(
				Constants.INTENT_KEY.BOOLEAN_VALUE, false);

		baseBl = new BaseOperationsBL(getHelper());
		friendsBL = new FriendsBL(getHelper());
		String currID = getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.CURRENT_ID, null);
		List<User> pending = friendsBL.getFriendByUserPending(currID);

		if (pending != null) {
			PreferencesUtils.setString(this, R.string.friends,
					String.valueOf(pending.size()));
		}

		List<Conversation> conv = baseBl.getListFromDBBy2Field(
				Conversation.class, Conversation.HAS_UNREAD_MESSAGES, true,
				Conversation.CURR_USER_ID, currID);
		if (conv != null) {
			PreferencesUtils.setString(this, R.string.notify_msg,
					String.valueOf(conv.size()));
		}
		// notify for unread messages
		stringNotify = PreferencesUtils.getString(this, R.string.notify_msg,
				"0");
		mTabs = getTabHost();
		View indicator;
		TabHost.TabSpec tab = mTabs.newTabSpec(FEED_TAG);
		Intent intent = new Intent(this, TabActivityFeed.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_feed,
				getString(R.string.tab_feed_txt), null);
		tab.setIndicator(indicator);
		mTabs.addTab(tab);

		tab = mTabs.newTabSpec(CONVERSATION_TAG);
		intent = new Intent(this, ActivityConversation.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_msgs,
				getString(R.string.msgs), stringNotify);
		tab.setIndicator(indicator);
		mTabs.addTab(tab);

		// Fake tab for button RUN
		tab = mTabs.newTabSpec(FAKE_TAG);
		intent = new Intent(this, ActivitySetup.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_runs,
				getString(R.string.run_apper), null);
		indicator.setVisibility(View.INVISIBLE);
		tab.setIndicator(indicator);
		mTabs.addTab(tab);
		//
		tab = mTabs.newTabSpec(LOG_TAG);
		intent = new Intent(this, ActivityLog.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_log, getString(R.string.log),
				null);
		tab.setIndicator(indicator);
		mTabs.addTab(tab);

		tab = mTabs.newTabSpec(SETUP_TAG);
		intent = new Intent(this, ActivitySetup.class);
		tab.setContent(intent);
		indicator = prepareTabView(R.drawable.tab_setup,
				getString(R.string.setup), null);
		tab.setIndicator(indicator);
		mTabs.addTab(tab);

		if (!isLog) {
			mTabs.setCurrentTab(0);
		} else {
			mTabs.setCurrentTab(logId);
		}

		findViewById(R.id.run).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(TabActivityMain.this,
						TabActivityTrack.class), Constants.RESULT_CODE_TRACK);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.RESULT_CODE_TRACK
				&& requestCode == Constants.RESULT_CODE_TRACK && data != null) {
			setmTrackPath(data.getStringExtra(Constants.INTENT_KEY.TRACK_PATH));
			mTabs.setCurrentTab(logId);
		} else {
			setmTrackPath(null);
		}
		if (resultCode == com.athlete.Constants.RESULT_CODE_TAB
				&& requestCode == com.athlete.Constants.RESULT_CODE_TRACK) {
			mTabs.setCurrentTab(logId);
		}
	}

	private View prepareTabView(int drawableIcon, String text, String notify) {
		View view = getLayoutInflater().inflate(R.layout.tab, null);
		((ImageView) view.findViewById(R.id.imViewTab))
				.setImageResource(drawableIcon);
		((TextView) view.findViewById(R.id.tvTab)).setText(text);
		if (notify != null) {
			txtNotify = (TextView) view.findViewById(R.id.txtNotifyMsg);
			setTextNotify(notify);
		} else {
			view.findViewById(R.id.txtNotifyMsg).setVisibility(View.GONE);
		}
		return view;
	}

	public String getmTrackPath() {
		return mTrackPath;
	}

	public void setTextNotify(String notify) {
		if (!notify.equalsIgnoreCase("0")) {
			txtNotify.setVisibility(View.VISIBLE);
			txtNotify.setText(notify);
		} else {
			txtNotify.setVisibility(View.GONE);
		}
	}

	public void setmTrackPath(String mTrackPath) {
		this.mTrackPath = mTrackPath;
	}

}
