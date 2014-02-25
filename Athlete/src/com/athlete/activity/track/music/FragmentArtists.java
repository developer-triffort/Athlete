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

public class FragmentArtists extends BaseFragmentAlbum {
	private int artistCursorPos;

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

						// play all music
						if (mUri.equals(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI)) {
							setDataForMusicWithoutPlayList();
						}
						// play all album by artist
						if (mUri.equals(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)) {

							mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

							String[] projection = { MediaStore.Audio.Media._ID,
									MediaStore.Audio.Media.DATA,
									MediaStore.Audio.Media.ARTIST,
									MediaStore.Audio.Media.TITLE,
									MediaStore.Audio.Media.ALBUM_ID };
							mProjection = projection;
						}
						playAll();
					}
				});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (mUri.equals(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI)) {
					artistCursorPos = arg2;
					setAndDisplayAlbums();
				} else {
					if (mUri.equals(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)) {
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
						txtTitle.setText(mCursor.getString(music_column_index));
						mSelection = android.provider.MediaStore.Audio.Media.ALBUM
								+ "=?";
						whereVal = whereValue;
						orderBy = android.provider.MediaStore.Audio.Media.TITLE;
						mProjection = projection;
						setListViewAdapterSongs(false);
					}
				}
			}
		});

		resumeData();
		setArtistAdapter();

		return view;
	}

	private void setAndDisplayAlbums() {
		mCursor.moveToPosition(artistCursorPos);
		int music_column_index = mCursor
				.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		String[] whereValue = { mCursor.getString(music_column_index) };

		String[] projection = { MediaStore.Audio.Albums._ID,

		MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST };

		mProjection = projection;
		mSelection = MediaStore.Audio.Artists.ARTIST + "=?";
		whereVal = whereValue;
		mUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		orderBy = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
		String[] from = new String[] { MediaStore.Audio.Albums.ALBUM,
				MediaStore.Audio.Albums.ARTIST };

		int[] to = new int[] { R.id.songname, R.id.rowartist };
		mFrom = from;
		mTo = to;
		albumScreen();
		setListViewAlbumAdapter(false);
	}

	@Override
	public boolean onBackPressed() {
		if ((BaseFragment) getActivity() == null
				|| (mTo != null
						&& mUri.equals(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI) && ((BaseFragment) getActivity()).indicator
						.getVisibility() == View.VISIBLE)) {
			return true;
		} else {
			if (mUri.equals(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)) {
				artistScreen();
				resumeData();
				setArtistAdapter();
			} else {
				if (mUri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
					resumeData();
					if (mCursor != null) {
						mCursor.close();
					}
					mCursor = getActivity().getContentResolver().query(mUri,
							mProjection, mSelection, whereVal, orderBy);
					setAndDisplayAlbums();
				}
			}
		}
		return false;
	}

	private void resumeData() {
		String[] projection = { MediaStore.Audio.Artists._ID,
				MediaStore.Audio.Artists.ARTIST,
				MediaStore.Audio.Artists.ARTIST_KEY };

		mProjection = projection;
		mSelection = null;
		whereVal = null;
		mUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		orderBy = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;
		mFrom = new String[] { MediaStore.Audio.Artists.ARTIST };
		mTo = new int[] { R.id.songname };

	}
}
