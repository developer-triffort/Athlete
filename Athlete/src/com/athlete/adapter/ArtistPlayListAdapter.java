package com.athlete.adapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
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

public class ArtistPlayListAdapter extends CursorAdapter {
	private ContentResolver res;
	private HashMap<String, List<Integer>> idAlbum = new HashMap<String, List<Integer>>();
	private HashSet<Integer> audioID;
	private Activity activity;
	private Uri mUri;
	private AsyncTask<String, String, String> progressTask;

	@SuppressWarnings("deprecation")
	public ArtistPlayListAdapter(Activity activity, Cursor c,
			HashSet<Integer> audioID, Uri mUri) {
		super(activity, c);
		this.res = activity.getContentResolver();
		this.activity = activity;
		this.audioID = audioID;
		this.mUri = mUri;

	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public void bindView(final View view, final Context context, Cursor cursor) {

		int music_column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ARTIST);
		final String title = cursor.getString(music_column_index);
		String[] whereValue = { title };

		((TextView) view.findViewById(R.id.tvTitle)).setText(title);
		view.findViewById(R.id.tvArtist).setVisibility(View.GONE);
		view.findViewById(R.id.imViewAlbum).setVisibility(View.GONE);

		if (!idAlbum.containsKey(title)) {

			String[] projection = { MediaStore.Audio.Playlists.Members.AUDIO_ID };
			String mSelection = MediaStore.Audio.Playlists.Members.ARTIST
					+ "=?";
			Cursor mCursor = res.query(mUri, projection, mSelection,
					whereValue, null);
			idAlbum.put(title, getAllAudioId(mCursor));
			mCursor.close();
		}

		((ImageView) view.findViewById(R.id.btnX))
				.setImageDrawable(context.getResources().getDrawable(
						R.drawable.btn_remove_from_running));

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

									if (audioID.containsAll(idAlbum.get(title))) {
										// remove
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
									return null;

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
			albumsId.add(mCursor.getInt(0));
		}
		return albumsId;
	}
}
