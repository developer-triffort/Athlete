package com.athlete.activity.track;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.athlete.R;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.UserBL;
import com.athlete.control.PlaySound;
import com.athlete.db.DatabaseHelper;
import com.athlete.google.android.apps.mytracks.TrackController;
import com.athlete.google.android.apps.mytracks.content.TrackDataHub;
import com.athlete.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.athlete.google.android.apps.mytracks.util.TrackRecordingServiceConnectionUtils;
import com.athlete.model.ProfileUser;
import com.athlete.model.Split;
import com.athlete.model.User;
import com.athlete.util.AnalyticsUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseTabActivity;

public class BaseTabTrack extends OrmLiteBaseTabActivity<DatabaseHelper> {
	protected UserBL userBL;
	protected BaseOperationsBL baseOperationsBL;
	protected TrackDataHub trackDataHub;
	protected TrackController trackController;
	public static final String EXTRA_TRACK_ID = "track_id";
	protected long recordingTrackId = -1L, prevMaxMovingTime;
	protected TrackRecordingServiceConnection trackRecordingServiceConnection;
	protected Location lastLocation, prevLocation, lastLocationPause;
	protected TabHost tabs;
	protected boolean isStop = true, isAutopause, startNewRecording,
			recordingTrackPaused, isMetric, isAutoPauseCheck;
	protected LinearLayout linearGPS;
	protected LinearLayout.LayoutParams layoutGPSParams;
	protected Timer mTimerAutoPause, mTimerCountDown;
	protected int mSecond, minRecordingDistance, minRequiredAccuracy, time = 0,
			countGPSPosition = 0;
	protected ActivityMapTrack trackMapTrack;
	protected ActivitySplits activitySplits;
	protected SharedPreferences baseShared;
	protected TextView txtCountDown;
	protected MediaPlayer mSoundPlayer;
	protected HashMap<Integer, Integer> rawIDforTime;
	protected final String defType = "raw";
	protected Split mSplit;
	protected List<Split> mSplits;
	protected double distanceForRunUpdate, ONE_VALUE, prevMaxTotalDistance,
			firstElevation;
	protected boolean isNotification;
	protected User currenUser;
	protected ProfileUser profileUser;
	protected static final int CAMERA_REQUEST = 1888;
	protected static final int CAMERA_GALLERY = 2;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".PNG";
	protected AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private byte[] bytePhoto;
	private File file;
	private String mCurrentPhotoPath;
	protected boolean pauseAfterPhoto;

	protected void addPhoto() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = setUpPhotoFile();
			setCurrentPhotoPath(f.getAbsolutePath());
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			setCurrentPhotoPath(null);
		}
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	protected File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		setCurrentPhotoPath(f.getAbsolutePath());

		return f;
	}

	protected File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	protected String getAlbumName() {
		return getString(R.string.album_name);
	}

	protected File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());
			if (storageDir != null && !storageDir.mkdirs()
					&& !storageDir.exists()) {

				return null;
			}
		}

		return storageDir;
	}

	private void addGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, CAMERA_GALLERY);
	}

	protected void handleBigCameraPhoto() {
		if (getCurrentPhotoPath() != null) {
			galleryAddPic();
			setCurrentPhotoPath(null);
		}
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(getCurrentPhotoPath());
		setFile(f);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);

	}

	protected void addPictureMode() {
		final String[] items = getAvaMode();
		AlertDialog.Builder builder = new AlertDialog.Builder(BaseTabTrack.this);
		builder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						if (item == 0) {
							if (isSelectedTrackRecording()
									&& !isSelectedTrackPaused()) {
								recording2paused();
								pauseAfterPhoto = true;
							}
							addPhoto();
						} else {
							if (isSelectedTrackRecording()
									&& !isSelectedTrackPaused()) {
								recording2paused();
								pauseAfterPhoto = true;
							}
							addGallery();
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected synchronized boolean isSelectedTrackRecording() {
		return trackDataHub != null && trackDataHub.isSelectedTrackRecording();
	}

	/**
	 * Returns true if the selected track is paused. Needs to be synchronized
	 * because trackDataHub can be accessed by multiple threads.
	 */
	protected synchronized boolean isSelectedTrackPaused() {
		return trackDataHub != null && trackDataHub.isSelectedTrackPaused();
	}

	protected void paused2resume() {
		AnalyticsUtils.sendPageViews(BaseTabTrack.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.TRACK_SCREEN,
				AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
				AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
				AnalyticsUtils.GOOGLE_ANALYTICS.RESUME_TRACK, 0);
		// Paused -> Resume
		TrackRecordingServiceConnectionUtils
				.resumeTrack(trackRecordingServiceConnection);
		isAutopause = false;
		PlaySound.playSound(BaseTabTrack.this, mSoundPlayer,
				R.raw.trackingstarted, false);
	}

	protected void recording2paused() {
		AnalyticsUtils.sendPageViews(BaseTabTrack.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.TRACK_SCREEN,
				AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
				AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
				AnalyticsUtils.GOOGLE_ANALYTICS.PAUSE_TRACK, 0);
		if (mTimerAutoPause != null) {
			mTimerAutoPause.cancel();
		}
		trackController.stop();
		TrackRecordingServiceConnectionUtils
				.pauseTrack(trackRecordingServiceConnection);
		PlaySound.playSound(BaseTabTrack.this, mSoundPlayer,
				R.raw.trackingpaused, false);
	}

	private String[] getAvaMode() {
		String[] result = new String[] { getString(R.string.lbl_attach_photo),
				getString(R.string.lbl_attach_gallery) };
		return result;
	}

	public byte[] getBytePhoto() {
		return bytePhoto;
	}

	public void setBytePhoto(byte[] bytePhoto) {
		this.bytePhoto = bytePhoto;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getCurrentPhotoPath() {
		return mCurrentPhotoPath;
	}

	public void setCurrentPhotoPath(String mCurrentPhotoPath) {
		this.mCurrentPhotoPath = mCurrentPhotoPath;
	}
}

abstract class AlbumStorageDirFactory {
	public abstract File getAlbumStorageDir(String albumName);
}

final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

	// Standard storage location for digital camera files
	private static final String CAMERA_DIR = "/dcim/";

	@Override
	public File getAlbumStorageDir(String albumName) {
		return new File(Environment.getExternalStorageDirectory() + CAMERA_DIR
				+ albumName);
	}
}

final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

	@Override
	public File getAlbumStorageDir(String albumName) {
		// TODO Auto-generated method stub
		return new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				albumName);
	}
}
