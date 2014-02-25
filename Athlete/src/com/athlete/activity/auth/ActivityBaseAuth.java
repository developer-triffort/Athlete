package com.athlete.activity.auth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.TabActivityMain;
import com.athlete.control.SessionEvents;
import com.athlete.control.SessionEvents.AuthListener;
import com.athlete.control.SessionEvents.LogoutListener;
import com.athlete.control.SessionStore;
import com.athlete.model.ProfileUser;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.LoginTaskViaFB;
import com.athlete.services.task.get.GetMeTask;
import com.athlete.services.task.get.GetProfileTask;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class ActivityBaseAuth extends BaseActivity {
	/**
	 * @author edBaev
	 */
	protected final String GENDER = "gender";
	protected final String WEIGHT = "weight";
	protected final String WEIGHT_UNIT = "weight_unit";
	protected final String BIRTH_DAY = "birth_date";
	protected final String FIRST_NAME = "first_name";
	protected final String LAST_NAME = "last_name";
	protected Facebook mFacebook;
	protected AsyncFacebookRunner mAsyncRunner;
	protected SessionListener mSessionListener;
	protected boolean mFBClick, mIsLogin, isSaveTrack;
	protected SharedPreferences sp;
	protected String mCurrentPhotoPath;
	protected static final int CAMERA_REQUEST = 1888;
	protected static final int CAMERA_GALLERY = 2;
	protected static final String JPEG_FILE_PREFIX = "IMG_";
	protected static final String JPEG_FILE_SUFFIX = ".PNG";
	protected AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	protected ImageView mImageView;
	protected double weight;
	protected final int deffWeight = 150;
	protected String weightUnits;
	protected File file;
	protected byte[] bytePhoto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		restoreFB();
	}

	private void addPhoto() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			f = null;
			mCurrentPhotoPath = null;
		}
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());
			if (storageDir != null && !storageDir.mkdirs()
					&& !storageDir.exists()) {
				Log.d("CameraSample", "failed to create directory");
				return null;
			}
		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private String[] getAvaMode() {
		String[] result = new String[] { getString(R.string.lbl_attach_photo),
				getString(R.string.lbl_attach_gallery) };
		return result;
	}

	protected void selectAvatarMode() {
		final String[] items = getAvaMode();

		AlertDialog.Builder builder = new AlertDialog.Builder(
				ActivityBaseAuth.this);
		builder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						if (item == 0) {
							addPhoto();
						} else {
							addGallery();
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void addGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, CAMERA_GALLERY);
	}

	private void restoreFB() {
		mFacebook = new Facebook(Constants.APP_ID_FB);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		SessionStore.restore(mFacebook, this);
		mSessionListener = new SessionListener();
		SessionEvents.addAuthListener(mSessionListener);
		SessionEvents.addLogoutListener(mSessionListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.RESULT_CODE_TAB) {
			setResult(Constants.RESULT_CODE_TAB);
			finish();
			startTransferAnim();
		} else {
			if (mFBClick) {
				mFacebook.authorizeCallback(requestCode, resultCode, data);
				mAsyncRunner.request(Constants.FB_REQUEST_ME,
						new SampleRequestListener());
				mFBClick = false;
			}
		}
	}

	public class SampleRequestListener implements RequestListener {
		public void onComplete(final String response, final Object state) {
			final String TOKEN = "access_token";
			final String KEY = "facebook-session";
			SharedPreferences sp = getSharedPreferences(KEY,
					Context.MODE_PRIVATE);
			final String acces_token = sp.getString(TOKEN, null);

			getShared()
					.edit()
					.putString(Constants.SharedPreferencesKeys.FB_ACCES,
							acces_token).commit();
			if (!isSaveTrack)
				ActivityBaseAuth.this.runOnUiThread(new Runnable() {
					public void run() {
						loginViaFB(acces_token);
					}
				});

		}

		public void onFacebookError(FacebookError e, final Object state) {
			Log.e("Facebook", e.getMessage());
			e.printStackTrace();
		}

		public void onFileNotFoundException(FileNotFoundException e,
				final Object state) {
			Log.e("Facebook", e.getMessage());
			e.printStackTrace();
		}

		public void onIOException(IOException e, final Object state) {
			Log.e("Facebook", e.getMessage());
			e.printStackTrace();
		}

		public void onMalformedURLException(MalformedURLException e,
				final Object state) {
			Log.e("Facebook", e.getMessage());
			e.printStackTrace();
		}
	}

	public final class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
		}

		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onCancel() {
			SessionEvents.onLoginError("Action Canceled");
		}
	}

	private class SessionListener implements AuthListener, LogoutListener {
		public void onAuthSucceed() {
			SessionStore.save(mFacebook, ActivityBaseAuth.this);
		}

		public void onLogoutFinish() {
			SessionStore.clear(ActivityBaseAuth.this);
		}

		@Override
		public void onLogoutBegin() {

		}

		@Override
		public void onAuthFail(String error) {

		}
	}

	private void loginViaFB(final String acces_token) {

		final OnTskCpltListener loginTaskViaFB = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<String[]> result;
				try {
					result = (TaskResult<String[]>) task.get();
					if (result.isError()) {
						Toast.makeText(ActivityBaseAuth.this,
								result.getError_description(),
								Toast.LENGTH_SHORT).show();
					} else {

						sp = getSharedPreferences(Constants.PREFERENCES,
								Context.MODE_PRIVATE);
						sp.edit()
								.putString(
										Constants.SharedPreferencesKeys.USER_NAME,
										result.getResult()[0]).commit();
						sp.edit()
								.putString(
										Constants.SharedPreferencesKeys.FB_ACCES,
										acces_token).commit();
						sp.edit()
								.putString(
										Constants.SharedPreferencesKeys.API_KEY,
										result.getResult()[1]).commit();

						meTaskLogin(result.getResult()[0],
								result.getResult()[1]);

					}
				} catch (Exception e) {
				}
			}

		};

		LoginTaskViaFB task = new LoginTaskViaFB(ActivityBaseAuth.this,
				getURLHost(), getPublicKey(), getPrivateKey(), acces_token,
				mIsLogin, baseBl);
		getTaskManager().executeTask(task, loginTaskViaFB, null, true);
	}

	protected void meTaskLogin(final String userName, final String getApi_key) {

		final OnTskCpltListener meTask = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<User> result;
				try {
					result = (TaskResult<User>) task.get();
					if (result.isError()) {
						Toast.makeText(ActivityBaseAuth.this,
								result.getError_description(),
								Toast.LENGTH_SHORT).show();
					} else {

						sp = getSharedPreferences(Constants.PREFERENCES,
								Context.MODE_PRIVATE);
						userBL.createOrUpdate(result.getResult());
						sp.edit()
								.putString(
										Constants.SharedPreferencesKeys.CURRENT_ID,
										result.getResult().getId()).commit();

						getProfileTask(userName, getApi_key, result.getResult());
					}
				} catch (Exception e) {
					
				}
			}

		};
		GetMeTask getMeTask = new GetMeTask(ActivityBaseAuth.this,
				getURLHost(), getPublicKey(), getPrivateKey(), userName,
				getApi_key, baseBl);
		getTaskManager().executeTask(getMeTask, meTask, null, true);
	}

	protected void getProfileTask(final String userName,
			final String getApiKey, final User user) {

		final OnTskCpltListener profileTask = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<ProfileUser> result;
				try {
					result = (TaskResult<ProfileUser>) task.get();
					if (result.isError()) {
						Toast.makeText(ActivityBaseAuth.this,
								result.getError_description(),
								Toast.LENGTH_SHORT).show();
					} else {
						startApp();
					}
				} catch (Exception e) {
				}
			}

		};
		GetProfileTask getProfileTask = new GetProfileTask(
				ActivityBaseAuth.this, getURLHost(), getPublicKey(),
				getPrivateKey(), userName, getApiKey, user,
				getString(R.string.progress_title));
		getTaskManager().executeTask(getProfileTask, profileTask, null, true);
	}

	private void startApp() {

		startActivity(new Intent(ActivityBaseAuth.this, TabActivityMain.class));
		setResult(Constants.RESULT_CODE_TAB);
		finish();
		startTransferAnim();

	}

	protected void handleBigCameraPhoto() {
		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
		}
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = file = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private void setPic() {
		try {
			/*
			 * There isn't enough memory to open up more than a couple camera
			 * photos
			 */
			/* So pre-scale the target bitmap into which the file is decoded */

			/* Get the size of the ImageView */
			int targetW = mImageView.getWidth();
			int targetH = mImageView.getHeight();

			/* Get the size of the image */
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			/* Figure out which way needs to be reduced less */
			int scaleFactor = 1;
			if ((targetW > 0) || (targetH > 0)) {
				scaleFactor = Math.min(photoW / targetW, photoH / targetH);
			}

			/* Set bitmap options to scale the image decode target */
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			/* Decode the JPEG file into a Bitmap */
			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,
					bmOptions);

			/* Associate the Bitmap to the ImageView */
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			bytePhoto = stream.toByteArray();
			// bytePhoto = mCurrentPhotoPath;
			// String str = bytePhoto.toString();
			mImageView.setImageBitmap(bitmap);
			mImageView.setVisibility(View.VISIBLE);
		} catch (Exception e) {
		}
	}

	protected void meTask(String userName, String getApi_key) {
		final OnTskCpltListener meTask = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<User> result;
				try {

					result = (TaskResult<User>) task.get();
					if (result.isError()) {
						Toast.makeText(ActivityBaseAuth.this,
								result.getError_description(),
								Toast.LENGTH_SHORT).show();
					} else {
						sp = getSharedPreferences(Constants.PREFERENCES,
								Context.MODE_PRIVATE);
						userBL.createOrUpdate(result.getResult());
						sp.edit()
								.putString(
										Constants.SharedPreferencesKeys.CURRENT_ID,
										result.getResult().getId()).commit();
						startActivity(new Intent(
								ActivityBaseAuth.this,
								ActivitySignupDetails.class));
						setResult(Constants.RESULT_CODE_TAB);
						finish();
					}
				} catch (Exception e) {
				}
			}

		};
		GetMeTask getMeTask = new GetMeTask(ActivityBaseAuth.this,
				getURLHost(), getPublicKey(), getPrivateKey(), userName,
				getApi_key, baseBl);
		getTaskManager().executeTask(getMeTask, meTask, null, true);
	}

	abstract class AlbumStorageDirFactory {
		public abstract File getAlbumStorageDir(String albumName);
	}

	final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

		// Standard storage location for digital camera files
		private static final String CAMERA_DIR = "/dcim/";

		@Override
		public File getAlbumStorageDir(String albumName) {
			return new File(Environment.getExternalStorageDirectory()
					+ CAMERA_DIR + albumName);
		}
	}

	final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {
		@Override
		public File getAlbumStorageDir(String albumName) {
			return new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					albumName);
		}
	}
}
