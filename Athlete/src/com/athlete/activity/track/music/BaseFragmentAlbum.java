package com.athlete.activity.track.music;

import java.util.HashSet;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TabWidget;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.adapter.AlbumsAdapter;
import com.athlete.adapter.AlbumsPlayListAdapter;
import com.athlete.adapter.ArtistAdapter;
import com.athlete.adapter.ArtistPlayListAdapter;
import com.athlete.adapter.SongsAdapter;

public class BaseFragmentAlbum extends Fragment {
	protected Uri mUri;
	protected String[] mProjection;
	protected String mSelection;
	protected ListView mListView;
	protected Cursor mCursor;
	protected String orderBy;
	protected String whereVal[];
	protected HashSet<Integer> audioId;
	protected int playlistID;
	protected String[] mFrom;
	protected int[] mTo;
	protected TextView txtTitle;
	protected TabWidget tabWidget;

	public boolean onBackPressed() {

		return true;
	}

	protected void initData() {
		audioId = ((BaseFragment) getActivity()).getAudiID();
		txtTitle = ((BaseFragment) getActivity()).getTxtTitle();
		tabWidget = ((BaseFragment) getActivity()).getTabs();

	}

	protected void albumScreen() {
		tabWidget.setVisibility(View.INVISIBLE);
		txtTitle.setVisibility(View.VISIBLE);
		txtTitle.setText(mCursor.getString(mCursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ARTIST)));
		((BaseFragment) getActivity()).indicator.setVisibility(View.GONE);
		((BaseFragment) getActivity()).mPager.setPagingEnabled(false);
	}

	protected void artistScreen() {
		tabWidget.setVisibility(View.VISIBLE);
		txtTitle.setVisibility(View.GONE);

		((BaseFragment) getActivity()).indicator.setVisibility(View.VISIBLE);
		((BaseFragment) getActivity()).mPager.setPagingEnabled(true);
	}

	protected void playAll() {

		Intent intent = new Intent();
		intent.setData(mUri);
		intent.putExtra(Constants.MEDIA_PLAYER_KEY.PROJECTION_KEY, mProjection);
		intent.putExtra(Constants.MEDIA_PLAYER_KEY.SELECTION_KEY, mSelection);
		intent.putExtra(Constants.MEDIA_PLAYER_KEY.POSITION_KEY, 0);
		intent.putExtra(Constants.MEDIA_PLAYER_KEY.WHERE_KEY, whereVal);
		((BaseFragment) getActivity()).playAll(intent);
	}

	protected void setDateForAllMusicWithPlayList() {
		mProjection = null;
		mSelection = null;
		orderBy = null;
		whereVal = null;
	}

	protected void setDataForMusicWithoutPlayList() {
		String[] projection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID };
		mProjection = projection;
		mSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
		mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	}

	protected void setListViewAdapterSongs(boolean isPlaylist) {
		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = getActivity().getContentResolver().query(mUri, mProjection,
				mSelection, whereVal, orderBy);
		mListView.setAdapter(new SongsAdapter(getActivity(), mCursor, audioId,
				isPlaylist));
	}

	protected void setArtistAdapter() {
		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = getActivity().getContentResolver().query(mUri, mProjection,
				mSelection, whereVal, orderBy);

		mListView
				.setAdapter(new ArtistAdapter(getActivity(), mCursor, audioId));

	}

	protected void setArtistPlayListAdapter(Uri uri) {
		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = getActivity().getContentResolver().query(mUri, mProjection,
				mSelection, whereVal, orderBy);

		mListView.setAdapter(new ArtistPlayListAdapter(getActivity(), mCursor,
				audioId, uri));

	}

	protected void setListViewAlbumPlayListAdapter(boolean showArtist, Uri uri) {
		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = getActivity().getContentResolver().query(mUri, mProjection,
				mSelection, whereVal, orderBy);

		mListView.setAdapter(new AlbumsPlayListAdapter(getActivity(), mCursor,
				audioId, showArtist, uri));

	}

	protected void setListViewAlbumAdapter(boolean showArtist) {
		if (mCursor != null) {
			mCursor.close();
		}
		mCursor = getActivity().getContentResolver().query(mUri, mProjection,
				mSelection, whereVal, orderBy);

		mListView.setAdapter(new AlbumsAdapter(getActivity(), mCursor, audioId,
				showArtist));

	}
}
