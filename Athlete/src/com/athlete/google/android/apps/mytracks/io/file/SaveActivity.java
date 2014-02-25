package com.athlete.google.android.apps.mytracks.io.file;

import android.R.style;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.athlete.google.android.apps.mytracks.util.DialogUtils;

public class SaveActivity extends BaseActivity {

	public static final String EXTRA_TRACK_FILE_FORMAT = "track_file_format";
	public static final String EXTRA_TRACK_ID = "track_id";
	public static final String EXTRA_PLAY_TRACK = "play_track";

	private static final int DIALOG_PROGRESS_ID = 0;
	private static final int DIALOG_RESULT_ID = 1;

	private TrackFileFormat trackFileFormat;
	private long trackId;
	private boolean playTrack;
	private SaveAsyncTask saveAsyncTask;
	private ProgressDialog progressDialog;

	// result from the AsyncTask
	private boolean success;

	// message id from the AsyncTask
	private int messageId;

	// saved file path from the AsyncTask
	private String savedPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(style.Theme_Holo_Dialog_NoActionBar);
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		trackFileFormat = intent.getParcelableExtra(EXTRA_TRACK_FILE_FORMAT);
		trackId = intent.getLongExtra(EXTRA_TRACK_ID, -1L);
		playTrack = intent.getBooleanExtra(EXTRA_PLAY_TRACK, false);
		SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES,Context.MODE_PRIVATE);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,	null);
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);
		@SuppressWarnings("deprecation")
		Object retained = getLastNonConfigurationInstance();
		if (retained instanceof SaveAsyncTask) {
			saveAsyncTask = (SaveAsyncTask) retained;
			saveAsyncTask.setActivity(this);
		} else {
			saveAsyncTask = new SaveAsyncTask(this, trackFileFormat, trackId,playTrack);
			saveAsyncTask.execute();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		saveAsyncTask.setActivity(null);
		return saveAsyncTask;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS_ID:
			progressDialog = DialogUtils.createHorizontalProgressDialog(this,
					R.string.sd_card_save_progress_message,
					new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
						
						}
					});
			return progressDialog;
		case DIALOG_RESULT_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setCancelable(false)
					.setIcon(
							success ? android.R.drawable.ic_dialog_info
									: android.R.drawable.ic_dialog_alert)
					.setMessage(messageId)
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									dialog.dismiss();
									onPostResultDialog();
								}
							})
					.setPositiveButton(R.string.generic_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int arg1) {
									dialog.dismiss();
									onPostResultDialog();
								}
							})
					.setTitle(
							success ? R.string.generic_success_title
									: R.string.generic_error_title);

			if (success && trackId != -1L) {
				builder.setNegativeButton(R.string.share_track_share_file,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								finish();
							}
						});
			}
			return builder.create();
		default:
			return null;
		}
	}

	/**
	 * Invokes when the associated AsyncTask completes.
	 * 
	 * @param isSuccess
	 *            true if the AsyncTask is successful
	 * @param aMessageId
	 *            the id of the AsyncTask message
	 * @param aSavedPath
	 *            the path of the saved file
	 */

	@SuppressWarnings("deprecation")
	public void onAsyncTaskCompleted(boolean isSuccess, int aMessageId,
			String aSavedPath) {
		this.success = isSuccess;
		this.messageId = aMessageId;
		this.savedPath = aSavedPath;
		removeDialog(DIALOG_PROGRESS_ID);
		if (success) {

			setResult(com.athlete.Constants.RESULT_CODE_TRACK,
					new Intent().putExtra(Constants.INTENT_KEY.TRACK_PATH,
							savedPath));
			finish();

		}
	}

	/**
	 * Shows the progress dialog.
	 */
	@SuppressWarnings("deprecation")
	public void showProgressDialog() {
		showDialog(DIALOG_PROGRESS_ID);
	}

	/**
	 * Sets the progress dialog value.
	 * 
	 * @param number
	 *            the number of points saved
	 * @param max
	 *            the maximum number of points
	 */
	public void setProgressDialogValue(int number, int max) {
		if (progressDialog != null) {
			progressDialog.setIndeterminate(false);
			progressDialog.setMax(max);
			progressDialog.setProgress(Math.min(number, max));
		}
	}

	/**
	 * To be invoked after showing the result dialog.
	 */
	private void onPostResultDialog() {
		finish();
	}
}
