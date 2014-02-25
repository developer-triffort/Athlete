package com.athlete.activity.track.music;

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.Window;
import android.widget.TabWidget;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.R.style;
import com.athlete.control.CustomViewPager;
import com.athlete.control.viewpagerindicator.TitlePageIndicator;

public class BaseFragment extends FragmentActivity {
	protected final String[] titles = { "Artists", "Albums", "Songs" };
	protected int mPlaylistId;
	protected HashSet<Integer> audioId;
	protected List<Fragment> fragments;
	protected CustomViewPager mPager;
	protected TabWidget tabs;
	protected TextView txtTitle;
	protected TitlePageIndicator indicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTheme(style.Theme_PageIndicatorDefaults);
		setContentView(R.layout.fragment_running);

	}

	public HashSet<Integer> getAudiID() {
		return audioId;

	}

	@Override
	protected void onResume() {
		super.onResume();
		getParent().findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();

					}
				});
	}

	@Override
	public void onBackPressed() {
		if (((BaseFragmentAlbum) fragments.get(0)) != null
				&& ((BaseFragmentAlbum) fragments.get(1)) != null
				&& ((BaseFragmentAlbum) fragments.get(2)) != null
				&& ((BaseFragmentAlbum) fragments.get(0)).onBackPressed()
				& ((BaseFragmentAlbum) fragments.get(1)).onBackPressed()
				& ((BaseFragmentAlbum) fragments.get(2)).onBackPressed()) {
			super.onBackPressed();

		}

	}

	public void setAudioID(HashSet<Integer> audioId) {
		this.audioId = audioId;
	}

	protected SharedPreferences getShared() {
		SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE);
		return sp;
	}

	public void playAll(Intent intent) {
		getParent().setResult(RESULT_OK, intent);
		getParent().finish();

	}

	protected void setAdapter() {

		FragmentManager fm = getSupportFragmentManager();
		mPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			public int getCount() {
				return fragments.size();
			}

			public Fragment getItem(int position) {

				return fragments.get(position);
			}

			public int getItemPosition(Object item) {

				return POSITION_NONE;

			}

			@Override
			public CharSequence getPageTitle(int position) {
				return titles[position];
			}
		});
	}

	protected void getAllAudioId() {
		Cursor mCursor;
		Uri mUri;
		mUri = MediaStore.Audio.Playlists.Members.getContentUri("external",
				mPlaylistId);
		mCursor = getContentResolver().query(mUri, null, null, null, null);
		int column = mCursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
		while (mCursor.moveToNext()) {
			int id = mCursor.getInt(column);
			audioId.add(id);
		}
	}

	public void addToPlaylist(int audioId) {
		this.audioId.add(audioId);
		((TabActivityMusic) getParent()).addToPlaylist(audioId);
	}


	public void removeFromPlaylist(int audioId) {
		this.audioId.remove(audioId);
		((TabActivityMusic) getParent()).removeFromPlaylist(audioId);
	}

	public void addListToPlaylist(List<Integer> audioId) {
		this.audioId.addAll(audioId);
		((TabActivityMusic) getParent()).addListToPlaylist(audioId);
	}

	public void removeListFromPlaylist(List<Integer> audioId) {
		this.audioId.removeAll(audioId);
		((TabActivityMusic) getParent()).removeListFromPlaylist(audioId);
	}
	
	public TextView getTxtTitle() {
		return txtTitle;
	}

	public void setTxtTitle(TextView txtTitle) {
		this.txtTitle = txtTitle;
	}

	public TabWidget getTabs() {
		return tabs;
	}

	public void setTabs(TabWidget tabs) {
		this.tabs = tabs;
	}

}
