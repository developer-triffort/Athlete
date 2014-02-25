package com.athlete.adapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.R;
import com.athlete.activity.track.music.BaseFragment;

public class AlbumsPlayListAdapter extends CursorAdapter {
	private Bitmap artwork;
	private HashMap<Uri, Bitmap> hashMap = new HashMap<Uri, Bitmap>();
	private Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private ContentResolver res;
	private BitmapFactory.Options options = new Options();
	private boolean showArtist;
	private HashSet<Integer> audioID;
	private HashMap<String, List<Integer>> idAlbum = new HashMap<String, List<Integer>>();
	private Uri mUri;
	private Activity activity;
	private AsyncTask<String, String, String> progressTask;

	@SuppressWarnings("deprecation")
	public AlbumsPlayListAdapter(Activity activity, Cursor c,
			HashSet<Integer> audioID, boolean showArtist, Uri mUri) {
		super(activity, c);
		this.res = activity.getContentResolver();
		this.options.inSampleSize = 2;
		this.audioID = audioID;
		this.showArtist = showArtist;
		this.mUri = mUri;
		this.activity = activity;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public void bindView(final View view, final Context context, Cursor cursor) {
		artwork = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.album_default);

		int music_column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ALBUM);
		final String title = cursor.getString(music_column_index);

		((TextView) view.findViewById(R.id.tvTitle)).setText(title);
		((ImageView) view.findViewById(R.id.btnX))
				.setImageDrawable(context.getResources().getDrawable(
						R.drawable.btn_remove_from_running));
		if (showArtist) {
			music_column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ARTIST);
			final String artist = cursor.getString(music_column_index);
			((TextView) view.findViewById(R.id.tvArtist)).setText(artist);
		}

		music_column_index = cursor
				.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);
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

		if (!idAlbum.containsKey(title)) {
			String[] whereValue = { title };
			String mSelection = android.provider.MediaStore.Audio.Media.ALBUM
					+ "=?";
			Cursor mCursor = res
					.query(mUri, null, mSelection, whereValue, null);
			idAlbum.put(title, getAllAudioId(mCursor));
			mCursor.close();
		}

		view.findViewById(R.id.btnX).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						{
							
							progressTask = new AsyncTask<String, String, String>() {
								@Override
								protected void onPreExecute() {
									view.findViewById(R.id.progressBar)
											.setVisibility(View.VISIBLE);
									view.findViewById(R.id.btnX).setVisibility(
											View.INVISIBLE);
								}

								@Override
								protected String doInBackground(
										String... params) {

									StringBuffer stringBuffer = new StringBuffer();
									List<Integer> check = idAlbum.get(title);
									((BaseFragment) activity)
											.removeListFromPlaylist(check);
									stringBuffer.append(check.size() + " ");
									if (check.size() == 1) {
										stringBuffer.append(context
												.getString(R.string.track)
												+ " ");
									} else {
										stringBuffer.append(context
												.getString(R.string.tracks)
												+ " ");
									}

									stringBuffer.append(context
											.getString(R.string.toast_removed_from_playlist));

									return stringBuffer.toString();

								}

								@Override
								protected void onPostExecute(String result) {
									super.onPostExecute(result);
									if (result != null) {
										Toast.makeText(context, result,
												Toast.LENGTH_SHORT).show();
									}
									view.findViewById(R.id.progressBar)
											.setVisibility(View.GONE);
									view.findViewById(R.id.btnX).setVisibility(
											View.VISIBLE);
								}
							};
							progressTask.execute();

							notifyDataSetChanged();
						}
					}
				});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_playlist, null);
		return view;
	}

	private List<Integer> getAllAudioId(Cursor mCursor) {
		List<Integer> albumsId = new LinkedList<Integer>();
		while (mCursor.moveToNext()) {
			albumsId.add(mCursor.getInt(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
		}
		return albumsId;
	}
}
