package com.athlete.adapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
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

public class ArtistAdapter extends CursorAdapter {
	private ContentResolver res;
	private BitmapFactory.Options options = new Options();
	private HashMap<String, List<Integer>> idAlbum = new HashMap<String, List<Integer>>();
	private HashSet<Integer> audioID;
	private Activity activity;
	private AsyncTask<String, String, String> progressTask;

	@SuppressWarnings("deprecation")
	public ArtistAdapter(Activity activity, Cursor c, HashSet<Integer> audioID) {
		super(activity, c);
		this.res = activity.getContentResolver();
		this.activity = activity;
		this.options.inSampleSize = 2;
		this.audioID = audioID;

	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public void bindView(final View view, final Context context, Cursor cursor) {

		int music_column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		final String title = cursor.getString(music_column_index);
		String[] whereValue = { title };

		((TextView) view.findViewById(R.id.tvTitle)).setText(title);
		view.findViewById(R.id.tvArtist).setVisibility(View.GONE);
		view.findViewById(R.id.imViewAlbum).setVisibility(View.GONE);

		if (!idAlbum.containsKey(title)) {
			Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] projection = { MediaStore.Audio.Media._ID };
			String mSelection = MediaStore.Audio.Media.IS_MUSIC
					+ " != 0 ) AND (" + MediaStore.Audio.Artists.ARTIST + "=?";
			Cursor mCursor = res.query(mUri, projection, mSelection,
					whereValue, null);
			idAlbum.put(title, getAllAudioId(mCursor));
			mCursor.close();
		}
		if (audioID.containsAll(idAlbum.get(title))) {
			((ImageView) view.findViewById(R.id.btnX)).setImageDrawable(context
					.getResources().getDrawable(
							R.drawable.btn_remove_from_running));
		} else {
			((ImageView) view.findViewById(R.id.btnX)).setImageDrawable(context
					.getResources().getDrawable(R.drawable.btn_add_to_running));
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

									} else {
										// add
										check.removeAll(audioID);

										((BaseFragment) activity)
												.addListToPlaylist(check);
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
												.getString(R.string.toast_added_to_playlist));
									}
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
									progressTask.cancel(true);
									notifyDataSetChanged();
								}
							};
							progressTask.execute();

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
