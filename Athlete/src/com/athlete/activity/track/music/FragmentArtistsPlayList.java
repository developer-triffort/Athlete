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

public class FragmentArtistsPlayList extends BaseFragmentAlbum {
	private final int typeAlbum = 1;
	private final int typeSongs = 2;
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
						if (mTo != null && mTo.length == typeAlbum) {
							setDateForAllMusicWithPlayList();
						}
						// play all album by artist
						if (mTo != null && mTo.length == typeSongs) {
							mProjection = null;
							orderBy = null;
							mSelection = MediaStore.Audio.Playlists.Members.ARTIST_ID
									+ "=?";
						}
						playAll();
					}
				});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (mTo != null && mTo.length == typeAlbum) {
					artistCursorPos = arg2;
					clickItemForAlbum();
				} else {
					if (mTo != null && mTo.length == typeSongs) {
						mTo = null;
						mCursor.moveToPosition(arg2);
						int music_column_index = mCursor
								.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
						String[] whereValue = { mCursor
								.getString(music_column_index) };
						txtTitle.setText(mCursor.getString(music_column_index));
						mSelection = android.provider.MediaStore.Audio.Media.ALBUM
								+ "=?";
						whereVal = whereValue;
						orderBy = android.provider.MediaStore.Audio.Media.TITLE;
						mProjection = null;
						setListViewAdapterSongs(true);
					}
				}
			}
		});
		playlistID = getActivity().getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE).getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);
		clickItemForArtists();
		return view;
	}

	private void clickItemForArtists() {
		String[] projection = { MediaStore.Audio.Playlists.Members._ID,
				MediaStore.Audio.Playlists.Members.ARTIST,
				MediaStore.Audio.Playlists.Members.ARTIST_ID,
				MediaStore.Audio.Playlists.Members.ARTIST_KEY };
		mProjection = projection;
		mSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 "
				+ ") GROUP BY (" + MediaStore.Audio.Playlists.Members.ARTIST;
		whereVal = null;
		mUri = MediaStore.Audio.Playlists.Members.getContentUri("external",
				playlistID);
		orderBy = null;
		mTo = new int[] { R.id.songname };
		mFrom = new String[] { MediaStore.Audio.Playlists.Members.ARTIST };
		setArtistPlayListAdapter(mUri);
	}

	private void clickItemForAlbum() {
		if (mCursor.moveToPosition(artistCursorPos)) {

			int music_column_index = mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ARTIST_ID);
			String[] projection = { MediaStore.Audio.Playlists.Members._ID,
					MediaStore.Audio.Playlists.Members.ALBUM,
					MediaStore.Audio.Playlists.Members.ALBUM_ID,
					MediaStore.Audio.Playlists.Members.TITLE,
					MediaStore.Audio.Playlists.Members.ARTIST };
			mProjection = projection;
			String[] whereValue = { mCursor.getString(music_column_index) };
			mSelection = MediaStore.Audio.Playlists.Members.ARTIST_ID + "=?"
					+ ") GROUP BY (" + MediaStore.Audio.Playlists.Members.ALBUM;
			whereVal = whereValue;
			mFrom = new String[] { MediaStore.Audio.Playlists.Members.ALBUM,
					MediaStore.Audio.Playlists.Members.ARTIST };
			mTo = new int[] { R.id.songname, R.id.rowartist };
			orderBy = MediaStore.Audio.Albums.ALBUM;
			albumScreen();
			setListViewAlbumPlayListAdapter(false, mUri);
		} else {
			artistScreen();
			clickItemForArtists();
		}
	}

	@Override
	public boolean onBackPressed() {

		if ((BaseFragment) getActivity() == null
				|| (mTo != null && mTo.length == typeAlbum && ((BaseFragment) getActivity()).indicator
						.getVisibility() == View.VISIBLE)) {
			return true;
		} else {
			if (mTo != null && mTo.length == typeSongs) {
				clickItemForArtists();
				artistScreen();
			} else if (mTo == null) {

				String[] projection = { MediaStore.Audio.Playlists.Members._ID,
						MediaStore.Audio.Playlists.Members.ARTIST,
						MediaStore.Audio.Playlists.Members.ARTIST_ID,
						MediaStore.Audio.Playlists.Members.ARTIST_KEY };

				mProjection = projection;
				mSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 "
						+ ") GROUP BY ("
						+ MediaStore.Audio.Playlists.Members.ARTIST;
				whereVal = null;
				mUri = MediaStore.Audio.Playlists.Members.getContentUri(
						"external", playlistID);
				orderBy = null;
				mTo = new int[] { R.id.songname };
				mFrom = new String[] { MediaStore.Audio.Playlists.Members.ARTIST };
				if (mCursor != null) {
					mCursor.close();
				}
				mCursor = getActivity().getContentResolver().query(mUri,
						mProjection, mSelection, whereVal, orderBy);
				clickItemForAlbum();
			}
			return false;
		}

	}

}
