package com.athlete.activity.track.music;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.athlete.Constants;
import com.athlete.R;

public class FragmentAlbumsPlayList extends BaseFragmentAlbum {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.actv_playlist, container, false);
		initData();
		view.findViewById(R.id.playAll).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						// play all music from playlist
						if (whereVal == null) {
							setDateForAllMusicWithPlayList();
						}
						playAll();
					}
				});

		mListView = (ListView) view.findViewById(R.id.listViewMusic);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (whereVal == null || whereVal.length == 0) {

					mCursor.moveToPosition(arg2);
					int music_column_index = mCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
					String[] whereValue = { mCursor
							.getString(music_column_index) };

					mSelection = android.provider.MediaStore.Audio.Media.ALBUM
							+ "=?";
					whereVal = whereValue;
					orderBy = android.provider.MediaStore.Audio.Media.TITLE;
					mProjection = null;
					albumScreen();
					txtTitle.setText(mCursor.getString(music_column_index));
					setListViewAdapterSongs(true);
				}
			}
		});
		playlistID = getActivity().getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE).getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);
		setDataForDisplayAlbum();
		return view;
	}

	private void setDataForDisplayAlbum() {
		String[] projection = { MediaStore.Audio.Playlists.Members._ID,
				MediaStore.Audio.Playlists.Members.ALBUM,
				MediaStore.Audio.Playlists.Members.ALBUM_ID,
				MediaStore.Audio.Playlists.Members.TITLE,
				MediaStore.Audio.Playlists.Members.ARTIST };
		whereVal = null;
		mProjection = projection;
		orderBy = MediaStore.Audio.Albums.ALBUM;
		mSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 "
				+ ") GROUP BY (" + MediaStore.Audio.Playlists.Members.ALBUM;
		mUri = MediaStore.Audio.Playlists.Members.getContentUri("external",
				playlistID);

		setListViewAlbumPlayListAdapter(true, mUri);
	}

	@Override
	public boolean onBackPressed() {
		if (whereVal != null) {
			artistScreen();
			setDataForDisplayAlbum();

		} else {
			if (((BaseFragment) getActivity()).indicator.getVisibility() == View.VISIBLE) {
				return true;
			}
		}
		return false;
	}
}
