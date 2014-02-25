package com.athlete;

import java.util.Date;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.athlete.google.android.apps.mytracks.services.RemoveTempFilesService;
import com.athlete.google.android.apps.mytracks.util.ApiAdapterFactory;
import com.athlete.services.AsyncTaskManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.DecodingType;

@ReportsCrashes(formKey = "dGpBVjJCcnlQUlp4dmkwbHQtOFR4cXc6MA", logcatArguments = {
		"-t", "50", "Debug_tag :D" })
public class AthleteApplication extends Application {
	public static MediaPlayer mediaPlayerVoice;
	public static MediaPlayer mediaPlayerMusic;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.avatar).cacheInMemory().cacheOnDisc()
			.decodingType(DecodingType.MEMORY_SAVING).build();
	private String publicKey;
	private String privateKey;
	private String urlHost;
	private String[] pathes;
	private long offset = -1L;
	private Date date;

	@Override
	public void onCreate() {
		super.onCreate();
		if (Constants.DEBUG) {
			ApiAdapterFactory.getApiAdapter().enableStrictMode();
		}
		ACRA.init(AthleteApplication.this);

		Intent intent = new Intent(this, RemoveTempFilesService.class);
		startService(intent);
		mediaPlayerVoice = new MediaPlayer();
		mediaPlayerMusic = new MediaPlayer();
		ImageLoaderConfiguration config = ImageLoaderConfiguration
				.createDefault(AthleteApplication.this);
		imageLoader.init(config);
		updateHostAndKeys();

	}

	public void updateHostAndKeys() {
		setUrlHost(getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.SERVER,
				Constants.HOST.HOST_DEFF));

		setPublicKey(Constants.SIGNATURE.PUBLIC_KEY_MAIN);
		setPrivateKey(Constants.SIGNATURE.PRIVATE_KEY_MAIN);

	}

	public MediaPlayer getMediaPlayerVoice() {
		return mediaPlayerVoice;
	}

	public void createNewMediaPlayerVoiceObject() {
		mediaPlayerVoice = new MediaPlayer();
	}

	public MediaPlayer getMediaPlayerMusic() {
		return mediaPlayerMusic;
	}

	public void createNewMediaPlayerMusicObject() {
		mediaPlayerMusic = new MediaPlayer();
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public AsyncTaskManager getTaskManager(Context ctx) {
		return new AsyncTaskManager(ctx);
	}

	public DisplayImageOptions getDisplayImageOptions() {
		return options;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getUrlHost() {
		return urlHost;
	}

	public void setUrlHost(String urlHost) {
		this.urlHost = urlHost;
	}

	public String[] getPathes() {
		return pathes;
	}

	public void setPathes(String[] pathes) {
		this.pathes = pathes;
	}

	public Date getDate() {

		return date;
	}

	public long getTime() {
		return (new Date().getTime() + offset);
	}

	public void setDate(Date date) {

		this.date = date;
		offset = new Date().getTime() - date.getTime();
	}

}
