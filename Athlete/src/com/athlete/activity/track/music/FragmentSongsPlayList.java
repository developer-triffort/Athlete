package com.athlete.activity.track.music;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.athlete.Constants;
import com.athlete.R;

public class FragmentSongsPlayList extends BaseFragmentAlbum {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.actv_playlist, container, false);
		initData();
		
		mListView = (ListView) view.findViewById(R.id.listViewMusic);
		view.findViewById(R.id.playAll).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						playAll();

					}
				});
		playlistID = getActivity().getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE).getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);
		mSelection = null;
		mUri = MediaStore.Audio.Playlists.Members.getContentUri("external",
				playlistID);
		

		setListViewAdapterSongs(true);
		return view;
	}

}
