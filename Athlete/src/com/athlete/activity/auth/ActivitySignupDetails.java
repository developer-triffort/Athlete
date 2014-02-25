package com.athlete.activity.auth;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.model.ProfileUser;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SendPhotoTask;
import com.athlete.services.task.SignUpDetailsTask;
import com.athlete.util.AnalyticsUtils;

public class ActivitySignupDetails extends ActivityBaseAuth implements
		OnTskCpltListener {
	/**
	 * @author edBaev
	 */
	private Button mBtnGender, mBtnWeight, mBtnDB, mBtnLetsgo;
	private String gender;

	private String birthDate;

	private String mUserName;
	private String mApiKey;
	private String mId;
	private User currentUser;
	private ProfileUser profileUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_signup_details);
		AnalyticsUtils.sendPageViews(ActivitySignupDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SIGNUP_DETAILS);
		setView();
		SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES,
				Context.MODE_PRIVATE);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);
		mImageView = (ImageView) findViewById(R.id.imVTest);

		currentUser = userBL.getBy(mId);
		profileUser = baseBl.getFromDBByField(ProfileUser.class,
				ProfileUser.ID, currentUser.getProfileID());
		if (profileUser == null) {
			profileUser = new ProfileUser();
		}
	}



	private void setView() {
		mBtnGender = (Button) findViewById(R.id.btnGender);
		mBtnWeight = (Button) findViewById(R.id.btnWeight);
		mBtnDB = (Button) findViewById(R.id.btnDateOfBirthday);

		mBtnLetsgo = (Button) findViewById(R.id.btnLetsGo);
		gender = "F";
		weightUnits = Constants.WEIGHT_UNIT.POUNDS;
		findViewById(R.id.layoutProfilePicture).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						selectAvatarMode();
					}
				});
		findViewById(R.id.btnWhySo).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						startActivity(new Intent(ActivitySignupDetails.this,
								ActivitySignupDetailsWhy.class));
						startTransferAnim();

					}
				});
		mBtnGender.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ActivitySignupDetails.this,
						ActivitySignupDetailsGender.class),
						Constants.REQUEST_CODE_FEMALE);
			}
		});
		mBtnDB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ActivitySignupDetails.this,
						ActivitySignupDetailsBD.class),
						Constants.REQUEST_CODE_FEMALE);
			}
		});
		mBtnWeight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				double weightExtra;
				if (weight == 0) {
					weightExtra = deffWeight;
				} else {
					weightExtra = weight;
				}
				startActivityForResult(
						new Intent(ActivitySignupDetails.this,
								ActivitySignupDetailsWeight.class).putExtra(
								Constants.INTENT_KEY.WEIGHT, weightExtra)
								.putExtra(Constants.INTENT_KEY.UNIT,
										weightUnits),
						Constants.REQUEST_CODE_FEMALE);
			}
		});
		mBtnLetsgo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject jsonObjSend = new JSONObject();
				try {
					jsonObjSend.put(GENDER, gender);
					jsonObjSend.put(WEIGHT, String.valueOf(weight));
					jsonObjSend.put(WEIGHT_UNIT, weightUnits);
					jsonObjSend.put(BIRTH_DAY, birthDate);
				} catch (JSONException e) {

				}
				SignUpDetailsTask detailsTask = new SignUpDetailsTask(
						ActivitySignupDetails.this, getURLHost(),
						getPublicKey(), getPrivateKey(), mApiKey, mUserName,
						currentUser.getProfileID(), jsonObjSend);
				getTaskManager().executeTask(detailsTask,
						ActivitySignupDetails.this, null, true);

			}

		});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Constants.REQUEST_CODE_WEIGHT
				&& data.getStringExtra(Constants.INTENT_KEY.WEIGHT) != null) {
			StringBuffer buffer = new StringBuffer();
			weight = Double.valueOf(data
					.getStringExtra(Constants.INTENT_KEY.WEIGHT));
			weightUnits = data.getStringExtra(Constants.INTENT_KEY.UNIT);
			buffer.append(data.getStringExtra(Constants.INTENT_KEY.WEIGHT));
			buffer.append(" ");
			if (weightUnits.equals(Constants.WEIGHT_UNIT.POUNDS)) {
				buffer.append(getString(R.string.weight_lbs));
			} else {
				buffer.append(getString(R.string.weight_kg));
			}
			mBtnWeight.setText(buffer);
		}

		if (resultCode == Constants.REQUEST_CODE_BD
				&& data.getStringExtra(Constants.INTENT_KEY.BD) != null) {
			String string = data.getStringExtra(Constants.INTENT_KEY.BD);
			string = string.substring(0, 10);
			mBtnDB.setText(string);
			birthDate = data.getStringExtra(Constants.INTENT_KEY.BD);
		}

		if (resultCode == Constants.REQUEST_CODE_FEMALE) {
			mBtnGender.setText(getResources().getString(R.string.btn_female));
			gender = "F";
		}
		if (resultCode == Constants.REQUEST_CODE_MALE) {
			mBtnGender.setText(getResources().getString(R.string.btn_male));
			gender = "M";
		}
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			handleBigCameraPhoto();
		} else {
			if (requestCode == CAMERA_GALLERY && resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				if (cursor == null)
					return;
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				mCurrentPhotoPath = filePath;
				handleBigCameraPhoto();
			}
		}
	}

	private void uploadPhoto() {

		OnTskCpltListener upload = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.getResult()) {
						getProfileTask(getUserName(), getApikey(), currentUser);
					}
				} catch (Exception e) {

				}
			}
		};
		SendPhotoTask sendPhotoTask = new SendPhotoTask(
				ActivitySignupDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), file, mApiKey, mUserName);
		getTaskManager().executeTask(sendPhotoTask, upload, null, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskComplete(@SuppressWarnings("rawtypes") BaseTask task) {
		TaskResult<Boolean> result;
		try {
			result = (TaskResult<Boolean>) task.get();
			if (result.isError()) {
				String toast;
				if (result.getError_description() != null
						&& result.getError_description().length() > 0) {
					toast = result.getError_description();
				} else {
					toast = getString(R.string.toast_non_internet);
				}
				Toast.makeText(ActivitySignupDetails.this, toast,
						Toast.LENGTH_SHORT).show();
			} else {
				if (result.getResult()) {

					if (bytePhoto != null) {
						uploadPhoto();
					} else {
						getProfileTask(getUserName(), getApikey(), currentUser);
					}
				}
			}

		} catch (Exception e) {
		}
	}
}
