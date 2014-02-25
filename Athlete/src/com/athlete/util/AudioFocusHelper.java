package com.athlete.util;

import com.athlete.AthleteApplication;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioFocusHelper implements OnAudioFocusChangeListener {
	private static final String TAG = "AudioFocusHelper";
	private AudioManager mAudioManager;
	private MediaPlayer mediaPlayer;
	private int focus = 0;
	private final int focusTypeGain = 0;
	private final int focusTypeLoss = 1;
	private final int focusTypeTransient = 2;
	private final int focusTypeTransientDuck = 3;
	private boolean isVoice;

	public AudioFocusHelper(Context ctx, MediaPlayer mediaPlayer,
			boolean isVoice) {
		this.mediaPlayer = mediaPlayer;
		this.isVoice = isVoice;
		mAudioManager = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);
	}

	public synchronized boolean requestFocusMayDuck() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager
				.requestAudioFocus(this, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
						AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

	}

	public synchronized boolean requestFocus() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager
				.requestAudioFocus(this, AudioManager.STREAM_VOICE_CALL,
						AudioManager.AUDIOFOCUS_GAIN);

	}

	public synchronized boolean abandonFocus() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager
				.abandonAudioFocus(this);
	}

	public int getFocus() {
		return focus;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			Log.v(TAG, "onAudioFocusChange : AUDIOFOCUS_GAIN");
			if (mediaPlayer != null) {
				Log.v(TAG, "mediaPlayer : AUDIOFOCUS_GAIN");
				mediaPlayer.setVolume(1f, 1f);
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.start();
				}
			}

			focus = focusTypeGain;
			break;
		case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
			Log.v(TAG,
					"onAudioFocusChange : AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");

			break;
		case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
			Log.v(TAG, "onAudioFocusChange : AUDIOFOCUS_GAIN_TRANSIENT");

			break;
		case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
			Log.v(TAG, "onAudioFocusChange : AUDIOFOCUS_REQUEST_FAILED");

			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			Log.v(TAG, "onAudioFocusChange : AUDIOFOCUS_LOSS");

			if (!isVoice && mediaPlayer != null) {
				mediaPlayer.stop();
			}

			focus = focusTypeLoss;
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			Log.v(TAG, "onAudioFocusChange : AUDIOFOCUS_LOSS_TRANSIENT");
			if (!isVoice && mediaPlayer != null) {

				mediaPlayer.setVolume(0.2f, 0.2f);
			}
			focus = focusTypeTransient;

			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			Log.v(TAG,
					"onAudioFocusChange : AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
			if (!isVoice &&mediaPlayer != null) {
				mediaPlayer.setVolume(0.2f, 0.2f);
			}
			focus = focusTypeTransientDuck;
			break;
		}
	}
}
