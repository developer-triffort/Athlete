package com.athlete.activity.track.music;

import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.control.CustomViewPager;
import com.athlete.control.viewpagerindicator.TitlePageIndicator;
import com.athlete.util.AnalyticsUtils;

public class FragmentActivityAllMusic extends BaseFragment {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this,
				FragmentArtists.class.getName()));
		fragments
				.add(Fragment.instantiate(this, FragmentAlbums.class.getName()));
		fragments
				.add(Fragment.instantiate(this, FragmentSongs.class.getName()));
		mPager = (CustomViewPager) findViewById(R.id.pager);
		indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		setAdapter();
	
		indicator.setViewPager(mPager);
		mPlaylistId = getShared().getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);
		audioId = ((TabActivityMusic) getParent()).getAudiID();
		setAudioID(audioId);

		txtTitle = ((TabActivityMusic) getParent()).getTxtTitle();
		tabs = ((TabActivityMusic) getParent()).getTabWidget();

		setTxtTitle(txtTitle);
		setTabs(tabs);
		getAllAudioId();

	}
@Override
protected void onResume() {
	super.onResume();
	AnalyticsUtils.sendPageViews(FragmentActivityAllMusic.this, "AllMusicScreen");
}
}
