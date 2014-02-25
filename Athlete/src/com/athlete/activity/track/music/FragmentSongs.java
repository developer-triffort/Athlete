package com.athlete.activity.track.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.athlete.R;

public class FragmentSongs extends BaseFragmentAlbum {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.actv_playlist, container, false);
		initData();
		view.findViewById(R.id.playAll).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						playAll();

					}
				});
		mListView = (ListView) view.findViewById(R.id.listViewMusic);

		setDataForMusicWithoutPlayList();
		
		setListViewAdapterSongs(false);

		return view;
	}

}
