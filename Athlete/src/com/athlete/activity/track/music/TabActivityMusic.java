package com.athlete.activity.track.music;

import java.util.HashSet;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.R.color;
import com.athlete.db.DatabaseHelper;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseTabActivity;

public class TabActivityMusic extends OrmLiteBaseTabActivity<DatabaseHelper> {
	private TabHost tabs;
	private TextView txtTitle;
	private int mPlaylistId;
	private HashSet<Integer> audioId;
	private TabWidget tabWidget;
	private ContentResolver resolver;
	private Uri uri ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheme(android.R.style.Theme_Holo_NoActionBar);
		setContentView(R.layout.tab_music);

		resolver = getContentResolver();
		audioId = new HashSet<Integer>();
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		tabWidget = (TabWidget) findViewById(android.R.id.tabs);
		
		setupTabs();
		mPlaylistId = getShared().getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);
		 uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
					mPlaylistId);
		AnalyticsUtils.sendPageViews(TabActivityMusic.this, "PlayListScreen");
		
	}

	public HashSet<Integer> getAudiID() {
		return audioId;

	}

	public TextView getTxtTitle() {
		return txtTitle;

	}

	public TabWidget getTabWidget() {
		return tabWidget;

	}

	private SharedPreferences getShared() {
		SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE);
		return sp;
	}

	private void setBtnType(Button btn) {
		btn.setGravity(Gravity.CENTER);
		btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
		btn.setTextColor(getResources().getColorStateList(color.tab_feed));
		btn.setTypeface(null, Typeface.BOLD);
	}

	public void addToPlaylist(int audioId) {

		String[] cols = new String[] { "count(*)" };
	
		Cursor cur = resolver.query(uri, cols, null, null, null);
		cur.moveToFirst();
		final int base = cur.getInt(0);
		cur.close();
		ContentValues values = new ContentValues();
		values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,
				Integer.valueOf(base + audioId));
		values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
		resolver.insert(uri, values);
	}

	public void removeFromPlaylist(int audioId) {
		resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID
				+ " = " + audioId, null);
	}

	public void removeListFromPlaylist(List<Integer> audioId) {
		for (Integer id : audioId) {
			removeFromPlaylist(id);
		}
	}

	public void addListToPlaylist(List<Integer> audioId) {
		for (Integer id : audioId) {
			addToPlaylist(id);
		}
	}

	@SuppressWarnings("deprecation")
	private void setupTabs() {
		tabs = getTabHost();

		TabHost.TabSpec tab = tabs.newTabSpec(getString(R.string.btn_running));
		Intent intent = new Intent(this, FragmentActivityRunning.class);
		tab.setContent(intent);
		Button btnRunning = new Button(this);
		CommonHelper.setBackground(btnRunning,
				getResources().getDrawable(R.drawable.top_bar_tab_3));
		btnRunning.setText(getString(R.string.btn_running));
		setBtnType(btnRunning);
		tab.setIndicator(btnRunning);
		tabs.addTab(tab);

		tab = tabs.newTabSpec(getString(R.string.btn_all_music));
		intent = new Intent(this, FragmentActivityAllMusic.class);
		tab.setContent(intent);
		Button btnAllMusic = new Button(this);
		CommonHelper.setBackground(btnAllMusic,
				getResources().getDrawable(R.drawable.top_bar_tab_2));
		btnAllMusic.setText(getString(R.string.btn_all_music));
		setBtnType(btnAllMusic);
		tab.setIndicator(btnAllMusic);
		tabs.addTab(tab);
	}

}
