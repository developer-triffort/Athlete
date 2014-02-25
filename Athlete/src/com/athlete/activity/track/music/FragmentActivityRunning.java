package com.athlete.activity.track.music;

import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.control.CustomViewPager;
import com.athlete.control.viewpagerindicator.TitlePageIndicator;
import com.athlete.util.AnalyticsUtils;

public class FragmentActivityRunning extends BaseFragment {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this,
				FragmentArtistsPlayList.class.getName()));
		fragments.add(Fragment.instantiate(this,
				FragmentAlbumsPlayList.class.getName()));
		fragments.add(Fragment.instantiate(this,
				FragmentSongsPlayList.class.getName()));
		mPager = (CustomViewPager) findViewById(R.id.pager);
		audioId = ((TabActivityMusic) getParent()).getAudiID();
		setAudioID(audioId);
		txtTitle = ((TabActivityMusic) getParent()).getTxtTitle();
		tabs = ((TabActivityMusic) getParent()).getTabWidget();
		indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		setTxtTitle(txtTitle);

		setTabs(tabs);
		setAdapter();

		indicator.setViewPager(mPager);
		mPlaylistId = getShared().getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);

		getAllAudioId();

	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(FragmentActivityRunning.this,
				"RunningPlaylistScreen");
	}
}
