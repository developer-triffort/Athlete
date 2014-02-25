package com.athlete.activity.track.music;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.athlete.R;

public class FragmentAlbums extends BaseFragmentAlbum {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.actv_playlist, container, false);
		initData();

		view.findViewById(R.id.playAll).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						// play all music
						if (!mUri
								.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
							setDataForMusicWithoutPlayList();
						}
						// play music by album
						playAll();
					}
				});
		mListView = (ListView) view.findViewById(R.id.listViewMusic);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!mUri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {

					mCursor.moveToPosition(arg2);
					int music_column_index = mCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
					String[] whereValue = { mCursor
							.getString(music_column_index) };

					mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

					String[] projection = { MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.DATA,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media.ALBUM_ID };

					mSelection = android.provider.MediaStore.Audio.Media.ALBUM
							+ "=?";
					whereVal = whereValue;
					orderBy = android.provider.MediaStore.Audio.Media.TITLE;
					mProjection = projection;
					albumScreen();
					txtTitle.setText(mCursor.getString(music_column_index));

					setListViewAdapterSongs(false);
				}
			}
		});

		setDataForDisplayAlbum();
		setListViewAlbumAdapter(true);

		return view;
	}

	private void setDataForDisplayAlbum() {
		String[] projection = { MediaStore.Audio.Albums._ID,
				MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST };
		mProjection = projection;
		mSelection = null;
		mUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		orderBy = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
		mFrom = new String[] { MediaStore.Audio.Albums.ALBUM,
				MediaStore.Audio.Albums.ARTIST };
		whereVal = null;
		mTo = new int[] { R.id.songname, R.id.rowartist };
	}

	@Override
	public boolean onBackPressed() {
		if (whereVal != null) {
			artistScreen();
			setDataForDisplayAlbum();
			setListViewAlbumAdapter(true);
		} else {
			if (((BaseFragment) getActivity()).indicator.getVisibility() == View.VISIBLE) {
				return true;
			}
		}
		return false;
	}
}
