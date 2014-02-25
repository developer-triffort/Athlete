package com.athlete.adapter;

import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.R;
import com.athlete.activity.track.music.BaseFragment;

public class SongsAdapter extends CursorAdapter {
	private HashSet<Integer> audioID;
	private Activity activity;
	private Bitmap artwork;
	private boolean isPlayList;
	private HashMap<Uri, Bitmap> hashMap = new HashMap<Uri, Bitmap>();
	private Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private ContentResolver res;
	private BitmapFactory.Options options = new Options();

	@SuppressWarnings("deprecation")
	public SongsAdapter(Activity activity, Cursor c, HashSet<Integer> audioID,
			boolean isPlayList) {
		super(activity, c);
		this.audioID = audioID;
		this.activity = activity;
		this.isPlayList = isPlayList;
		this.res = activity.getContentResolver();
		this.options.inSampleSize = 2;

	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		artwork = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.album_default);
		int music_column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
		final String title = cursor.getString(music_column_index);
		((TextView) view.findViewById(R.id.tvTitle)).setText(title);

		music_column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
		((TextView) view.findViewById(R.id.tvArtist)).setText(cursor
				.getString(music_column_index));
		view.findViewById(R.id.tvArtist).setVisibility(View.VISIBLE);
		if (playlistHaveAudio(cursor)) {
			((ImageView) view.findViewById(R.id.btnX)).setImageDrawable(context
					.getResources().getDrawable(
							R.drawable.btn_remove_from_running));
		} else {
			((ImageView) view.findViewById(R.id.btnX)).setImageDrawable(context
					.getResources().getDrawable(R.drawable.btn_add_to_running));
		}
		final int column = playlistHaveAudioID(cursor);

		view.findViewById(R.id.btnX).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						{
							Log.d("audioID", String.valueOf(column));
							if (audioID.contains(column)) {
								((BaseFragment) activity)
										.removeFromPlaylist(column);
								Toast.makeText(
										context,
										"\""
												+ title
												+ "\" "
												+ context
														.getString(R.string.toast_removed_from_playlist),
										Toast.LENGTH_SHORT).show();
							} else {
								((BaseFragment) activity).addToPlaylist(column);
								Toast.makeText(
										context,
										"\""
												+ title
												+ "\" "
												+ context
														.getString(R.string.toast_added_to_playlist),
										Toast.LENGTH_SHORT).show();
							}
							audioID = ((BaseFragment) activity).getAudiID();
							notifyDataSetChanged();
						}
					}
				});
		music_column_index = cursor
				.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
		Uri uri = ContentUris.withAppendedId(sArtworkUri,
				cursor.getLong(music_column_index));
		if (!hashMap.containsKey(uri)) {
			try {

				java.io.InputStream in = res.openInputStream(uri);
				artwork = BitmapFactory.decodeStream(in, null, options);

			} catch (Exception e) {

			}
			hashMap.put(uri, artwork);
		} else {
			artwork = hashMap.get(uri);
		}
		((ImageView) view.findViewById(R.id.imViewAlbum))
				.setImageBitmap(artwork);
	}

	private boolean playlistHaveAudio(Cursor cursor) {
		if (isPlayList) {
			return audioID
					.contains(cursor.getInt(cursor
							.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
		} else {
			return audioID.contains(cursor.getInt(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
		}
	}

	private int playlistHaveAudioID(Cursor cursor) {
		if (isPlayList) {
			return cursor
					.getInt(cursor
							.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
		} else {
			return cursor.getInt(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_playlist, null);
		return view;
	}
}
