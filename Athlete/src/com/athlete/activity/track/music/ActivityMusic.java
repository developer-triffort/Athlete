package com.athlete.activity.track.music;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.track.TabActivityTrack;
import com.athlete.control.VolumeSeekBar;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.AudioFocusHelper;
import com.athlete.util.CommonHelper;

public class ActivityMusic extends BaseActivity {
	private final String TAG = "ActivityMusic";
	private ImageView imageViewAlbum;
	private TextView txtTrackName, txtArtistName, txtDuration;
	private ImageButton btnPlay, btnNext, btnVolume, btnPlayList;
	private ImageButton btnMix;
	private final String NEXT = "next";
	private final String PAUSE_OR_PLAY = "pauseOrPlay";
	private String mArtist;
	private String mTrack;
	private Cursor mMediaCursor;
	private int mMediaCursorPos = -1;
	private MediaPlayer mMediaPlayer;
	private AudioFocusHelper audioFocusHelper;
	private Notification notification;
	private NotificationManager mNotificationManager;
	private final int idNotification = 7;
	private Intent intentNext;
	private Intent intentPause;
	private NotificationCompat.Builder builder;
	private final int onMsLight = 200;
	private final int offMsLight = 600;
	private String trackName = null;
	private String artistName = null;
	private Bitmap artwork = null;
	private final Handler handler = new Handler();
	private int mPlaylistId;
	private final int oneSecond = 1000;
	private boolean isMix = false, isFinish;
	private VolumeSeekBar bar;
	private MusicReceiver next = new MusicReceiver();
	private MusicReceiver pause = new MusicReceiver();
	private int max;
	private AudioManager audioManager;
	private Timer mTimerAutoPause;
	private int mSecond;
	private final String PREF_PROJECTION = "pref_proj";
	private final String PREF_PROJECTION_COUNT = "pref_proj_count";
	private final String PREF_WHERE = "pref_where";
	private final String PREF_WHERE_COUNT = "pref_where_count";
	private final String PREF_SELECTION = "selection";
	private final String PREF_MEDIA_CUR_POS = "media_position";

	private String[] saveProjection;
	private String[] saveWhere;
	private String saveSelection;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.actv_music);
		mMediaPlayer = ((AthleteApplication) getApplication())
				.getMediaPlayerMusic();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		builder = new NotificationCompat.Builder(this).setLights(Color.BLUE,
				onMsLight, offMsLight).setOngoing(false);
		intentNext = new Intent(ALARM_SERVICE);
		intentNext.setAction(NEXT);
		intentPause = new Intent(AUDIO_SERVICE);
		intentPause.setAction(PAUSE_OR_PLAY);
		sharedPreferences = getShared();
		registerReceiver(next, new IntentFilter(NEXT));
		registerReceiver(pause, new IntentFilter(PAUSE_OR_PLAY));
		init();
		setOnclickButton();
		setTextView();
		mPlaylistId = getShared().getInt(
				Constants.SharedPreferencesKeys.PLAY_LIST_ID, -1);
		if (mPlaylistId == -1) {
			createPlayList();
		}
		mTimerAutoPause = new Timer();
		bar = (VolumeSeekBar) findViewById(R.id.volumeSeekBar);
		bar.setMax(max);
		bar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		if (bar.getProgress() > (int) (max / 2)) {
			CommonHelper.setBackground(bar,
					getResources().getDrawable(R.drawable.soundcontrol_down));
		} else {
			CommonHelper.setBackground(bar,
					getResources().getDrawable(R.drawable.soundcontrol_up));
		}

		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mSecond = 0;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mSecond = 0;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mSecond = 0;
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						progress, AudioManager.FLAG_SHOW_UI);
				if (progress > (int) (max / 2)) {
					CommonHelper.setBackground(seekBar, getResources()
							.getDrawable(R.drawable.soundcontrol_down));
				} else {
					CommonHelper.setBackground(seekBar, getResources()
							.getDrawable(R.drawable.soundcontrol_up));
				}
			}
		});
		playSavedMusic();
	}

	@SuppressWarnings("deprecation")
	private void playSavedMusic() {
		String stringUri = sharedPreferences.getString(
				Constants.SharedPreferencesKeys.URI, null);
		if (stringUri == null) {
			return;
		}
		Uri uri = Uri.parse(stringUri);
		if (uri == null) {
			return;
		}
		int projCount = sharedPreferences.getInt(PREF_PROJECTION_COUNT, 0);
		if (projCount != 0) {
			saveProjection = new String[projCount];
			for (int i = 0; i < projCount; i++) {
				saveProjection[i] = sharedPreferences.getString(PREF_PROJECTION
						+ "_" + i, null);
			}
		}
		int whereCount = sharedPreferences.getInt(PREF_WHERE_COUNT, 0);
		if (whereCount != 0) {
			saveWhere = new String[whereCount];
			for (int i = 0; i < whereCount; i++) {
				saveWhere[i] = sharedPreferences.getString(
						PREF_WHERE + "_" + i, null);
			}
		}
		saveSelection = sharedPreferences.getString(PREF_SELECTION, null);
		mMediaCursorPos = sharedPreferences.getInt(PREF_MEDIA_CUR_POS, 0);
		mMediaCursor = managedQuery(uri, saveProjection, saveSelection,
				saveWhere, null);

		playMusic();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityMusic.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.MUSIC);
	}

	private void createPlayList() {
		Cursor c;
		Uri mUri;
		ContentResolver resolver = getContentResolver();
		ContentValues mInserts = new ContentValues();
		mInserts.put(MediaStore.Audio.Playlists.NAME,
				getString(R.string.btn_running));
		mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED,
				System.currentTimeMillis());
		mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED,
				System.currentTimeMillis());
		mUri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
				mInserts);
		if (mUri != null) {
			c = resolver.query(mUri, PROJECTION_PLAYLIST, null, null, null);
			if (c != null) {
				c.moveToFirst();
				mPlaylistId = c.getInt(c
						.getColumnIndex(MediaStore.Audio.Playlists._ID));
				c.close();
			}
			getShared()
					.edit()
					.putInt(Constants.SharedPreferencesKeys.PLAY_LIST_ID,
							mPlaylistId).commit();
		}

	}

	public static final String[] PROJECTION_PLAYLIST = new String[] {
			MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME,
			MediaStore.Audio.Playlists.DATA };

	@Override
	public void onBackPressed() {
		isFinish = true;
		getParent().onBackPressed();
	}

	private void setAudioFocusHelper() {
		stopAudioFocus();
		if (android.os.Build.VERSION.SDK_INT >= 8) {
			audioFocusHelper = new AudioFocusHelper(ActivityMusic.this,
					mMediaPlayer, false);
			audioFocusHelper.requestFocus();
		} else {
			audioFocusHelper = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(next);
		unregisterReceiver(pause);
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		stopAudioFocus();
		if (mNotificationManager != null) {
			isFinish = true;
			mNotificationManager.cancel(idNotification);
		}
	}

	private void stopAudioFocus() {
		if (audioFocusHelper != null) {
			audioFocusHelper.abandonFocus();
		}
	}

	private void setTextView() {
		txtTrackName.setText(mTrack);
		txtArtistName.setText(mArtist);
		setDuration();
	}

	private void setDuration() {
		txtDuration.setText("");
	}

	private void setOnclickButton() {
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextSong();

			}
		});
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playOrPause();

			}
		});
		btnPlayList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ActivityMusic.this,
						TabActivityMusic.class),
						Constants.MEDIA_PLAYER_KEY.REQUEST_CODE);
			}
		});
		btnVolume.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startTimer();
				bar.setProgress(audioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC));
				if (bar.getProgress() > (int) (max / 2)) {
					CommonHelper.setBackground(
							bar,
							getResources().getDrawable(
									R.drawable.soundcontrol_down));
				} else {
					CommonHelper.setBackground(
							bar,
							getResources().getDrawable(
									R.drawable.soundcontrol_up));
				}
				bar.setVisibility(View.VISIBLE);
				btnVolume.setVisibility(View.INVISIBLE);

			}
		});
		btnMix.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isMix = !isMix;
				if (isMix) {

					btnMix.setImageResource(R.drawable.soundcontrol_3_active);
				} else {
					btnMix.setImageResource(R.drawable.soundcontrol_3);

				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.MEDIA_PLAYER_KEY.REQUEST_CODE
				&& resultCode == RESULT_OK) {
			Uri uri = data.getData();
			String[] proj = data
					.getStringArrayExtra(Constants.MEDIA_PLAYER_KEY.PROJECTION_KEY);
			String[] where = data
					.getStringArrayExtra(Constants.MEDIA_PLAYER_KEY.WHERE_KEY);
			String selection = data
					.getStringExtra(Constants.MEDIA_PLAYER_KEY.SELECTION_KEY);
			mMediaCursorPos = data.getIntExtra(
					Constants.MEDIA_PLAYER_KEY.POSITION_KEY, 0);
			mMediaCursor = managedQuery(uri, proj, selection, where, null);
			saveMediaCursorData(uri, proj, where, selection, 0);
			playMusic();
		}
	}

	private void saveMediaCursorData(Uri uri, String[] proj, String[] where,
			String selection, int pos) {

		if (proj != null) {
			for (int i = 0; i < proj.length; i++) {
				sharedPreferences.edit()
						.putString(PREF_PROJECTION + "_" + i, proj[i]).commit();
			}
			sharedPreferences.edit().putInt(PREF_PROJECTION_COUNT, proj.length)
					.commit();
		} else {
			sharedPreferences.edit().remove(PREF_PROJECTION_COUNT).commit();
		}

		if (where != null) {
			for (int i = 0; i < where.length; i++) {
				sharedPreferences.edit()
						.putString(PREF_WHERE + "_" + i, where[i]).commit();
			}
			sharedPreferences.edit().putInt(PREF_WHERE_COUNT, where.length)
					.commit();
		} else {
			sharedPreferences.edit().remove(PREF_WHERE_COUNT).commit();
		}
		if (selection != null) {
			sharedPreferences.edit().putString(PREF_SELECTION, selection)
					.commit();
		} else {
			sharedPreferences.edit().remove(PREF_SELECTION).commit();
		}
		if (uri != null) {
			sharedPreferences
					.edit()
					.putString(Constants.SharedPreferencesKeys.URI,
							uri.toString()).commit();
		} else {
			sharedPreferences.edit()
					.remove(Constants.SharedPreferencesKeys.URI).commit();
		}
		saveMediaCursorPosition(pos);
	}

	private void saveMediaCursorPosition(int pos) {
		sharedPreferences.edit().putInt(PREF_MEDIA_CUR_POS, pos).commit();
	}

	protected void playMusic() {
		if (mMediaCursor != null) {
			mMediaCursor.moveToPosition(mMediaCursorPos);
			if (mMediaCursor.getPosition() >= 0
					&& mMediaCursor.getPosition() < mMediaCursor.getCount()) {
				int columnIndex = mMediaCursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST);
				if (columnIndex != -1) {
					txtTrackName.setText(mMediaCursor.getString(columnIndex));
					trackName = mMediaCursor.getString(columnIndex);
				}
				columnIndex = mMediaCursor
						.getColumnIndex(MediaStore.Audio.Media.TITLE);
				if (columnIndex != -1) {
					txtArtistName.setText(mMediaCursor.getString(columnIndex));
					artistName = mMediaCursor.getString(columnIndex);
				}

				playMusic(mMediaCursor.getString(mMediaCursor
						.getColumnIndex(MediaStore.Audio.Media.DATA)));

				columnIndex = mMediaCursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

				Uri sArtworkUri = Uri
						.parse("content://media/external/audio/albumart");
				Uri uri = ContentUris.withAppendedId(sArtworkUri,
						mMediaCursor.getLong(columnIndex));
				ContentResolver res = getContentResolver();
				imageViewAlbum.setVisibility(View.VISIBLE);
				try {
					java.io.InputStream in = res.openInputStream(uri);
					artwork = BitmapFactory.decodeStream(in);
					imageViewAlbum.setImageBitmap(artwork);

				} catch (FileNotFoundException e) {
					artwork = null;
					imageViewAlbum.setImageDrawable(getResources().getDrawable(
							R.drawable.icon_big_play));
				}
				showNotification(trackName, artistName, artwork);
			}

		}

	}

	@SuppressWarnings("deprecation")
	public void showNotification(String trackName, String artistName,
			Bitmap artwork) {
		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification_layout);

		Intent intent = new Intent(this, TabActivityTrack.class).putExtra(
				com.athlete.Constants.INTENT_KEY.NOTIFICATION, true).setFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_ONE_SHOT);
		PendingIntent intentControl = PendingIntent.getBroadcast(this, 0,
				intentNext, 0);
		PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(this, 0,
				intentPause, 0);

		builder.setSmallIcon(R.drawable.icon_small_play);
		builder.setContentText(trackName).setContentTitle(artistName);

		if (!mMediaPlayer.isPlaying()) {
			contentView.setImageViewResource(R.id.btnPlay,
					R.drawable.button_music_play);

			btnPlay.setImageDrawable(getResources().getDrawable(
					R.drawable.button_music_play));
		} else {
			contentView.setImageViewResource(R.id.btnPlay,
					R.drawable.button_music_pause);
			btnPlay.setImageDrawable(getResources().getDrawable(
					R.drawable.button_music_pause));
		}
		contentView.setTextViewText(R.id.txtTrackName, trackName);
		contentView.setTextViewText(R.id.txtArtistName, artistName);
		contentView.setOnClickPendingIntent(R.id.btnNext, intentControl);
		contentView.setOnClickPendingIntent(R.id.btnPlay, pendingIntentPrev);
		if (artwork == null) {
			contentView.setImageViewResource(R.id.imaViewAlbum,
					R.drawable.icon_big_play);
		} else {
			contentView.setImageViewBitmap(R.id.imaViewAlbum, artwork);
		}
		builder.setContent(contentView);
		notification = builder.getNotification();

		notification.contentIntent = contentIntent;
		notification.flags = Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR | Notification.FLAG_SHOW_LIGHTS;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(idNotification, notification);
	}

	public void nextSong() {
		if (mMediaCursor != null) {
			if (isMix && mMediaCursor.getCount() > 2) {
				mMediaCursorPos = (int) (Math.random() * mMediaCursor
						.getCount());
				if (mMediaCursorPos == mMediaCursor.getPosition()) {
					nextSong();
					return;
				}
			} else {
				mMediaCursorPos++;
			}
			if (mMediaCursor.moveToPosition(mMediaCursorPos)) {
				playMusic();
			} else {
				mMediaCursor.moveToFirst();
				mMediaCursorPos = mMediaCursor.getPosition();
				playMusic();
			}
		}
		saveMediaCursorPosition(mMediaCursorPos);
	}

	protected void playMusic(String path) {
		if (path.length() != 0) {
			try {
				setAudioFocusHelper();
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.reset();
				mMediaPlayer.setDataSource(path);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
				mMediaPlayer
						.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								nextSong();
							}
						});
				updateCurrentDuration();
			} catch (Exception e) {
			}
		} else {
			mMediaPlayer.start();
		}
	}

	private void updateCurrentDuration() {
		txtDuration.setText(CommonHelper.getTimeMusic((int) mMediaPlayer
				.getCurrentPosition() / oneSecond));

		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			Runnable runnableCurrDuration = new Runnable() {
				public void run() {
					updateCurrentDuration();
				}
			};
			handler.postDelayed(runnableCurrDuration, oneSecond);
		} else {
			if (!isFinish) {
				showNotification(trackName, artistName, artwork);
			}
		}
	}

	private void init() {
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnMix = (ImageButton) findViewById(R.id.btnMix);
		btnVolume = (ImageButton) findViewById(R.id.btnVolume);
		btnPlayList = (ImageButton) findViewById(R.id.btnPlayList);
		imageViewAlbum = (ImageView) findViewById(R.id.imageViewAlbum);
		imageViewAlbum.setVisibility(View.GONE);
		txtArtistName = (TextView) findViewById(R.id.txtArtistName);
		txtTrackName = (TextView) findViewById(R.id.txtTrackName);
		txtDuration = (TextView) findViewById(R.id.txtDuration);
	}

	private void playOrPause() {
		if (mMediaCursor != null
				&& mMediaCursor.moveToPosition(mMediaCursorPos)) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();

			} else {
				mMediaPlayer.start();
				updateCurrentDuration();
			}
			showNotification(trackName, artistName, artwork);
		}
	}

	private void startTimer() {

		mTimerAutoPause.cancel();
		mSecond = 0;
		mTimerAutoPause = new Timer();
		mTimerAutoPause.schedule(new TimerTask() {
			@Override
			public void run() {
				mSecond++;
				if (mSecond >= 2) {
					mTimerAutoPause.cancel();
					bar.post(new Runnable() {
						@Override
						public void run() {
							bar.setVisibility(View.INVISIBLE);
						}
					});
					btnVolume.post(new Runnable() {
						@Override
						public void run() {
							btnVolume.setVisibility(View.VISIBLE);
						}
					});
				}
			}
		}, Constants.ONE_SECOND, Constants.ONE_SECOND);

	}

	public class MusicReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(NEXT)) {
				nextSong();
			}
			if (action.equals(PAUSE_OR_PLAY)) {
				playOrPause();
			}
		}
	}
}
