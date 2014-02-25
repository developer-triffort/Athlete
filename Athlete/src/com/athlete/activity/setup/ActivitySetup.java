package com.athlete.activity.setup;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.auth.ActivitySignupDetailsWeight;
import com.athlete.model.ProfileUser;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SendPhotoTask;
import com.athlete.services.task.get.GetMeTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

public class ActivitySetup extends BaseSetupActivity {
	/**
	 * @author edBaev
	 */
	private Button mBtnLogOut;
	private SharedPreferences sp;
	private LinearLayout mLinearMentricType, mLinearCountDount;
	private String mMetricType;
	private int mCountDown;
	private double mAudioTiming;
	private EditText mEditFullName;
	private final int deffCountdown = 0;
	private TextView mTxtMetricType, mTxtCountDount, mTxtAudioTiming,
			mTxtWeight, mTxtLocation, mTxtEmail;

	// private CheckBox chBoxAutoPause;

	private final int countOfFullName = 2;
	private final String updateUser = "/user/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_setup);
		setView();
		meTask();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySetup.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SETUP);
		setData();
	}

	private void setAudioTimming() {
		if (isMetric()) {
			mAudioTiming = sp.getFloat(
					Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
					Constants.AUDIO_TIMING_FLOAT.ONE);
		} else {
			mAudioTiming = sp.getFloat(Constants.INTENT_KEY.AUDIO_TIMING_MILE,
					Constants.AUDIO_TIMING_FLOAT.ZERO_POINT_FIVE);
		}
		if (mAudioTiming == -1) {
			mTxtAudioTiming.setText(getString(R.string.never));
		} else {
			mTxtAudioTiming.setText(getString(R.string.every)
					+ " "
					+ new DecimalFormat("0.#").format(mAudioTiming)
							.replace(",", ".").replace("0", "")
					+ " "
					+ (isMetric() ? getResources()
							.getString(R.string.kilometer) : getResources()
							.getString(R.string.mi)));
		}
	}

	private void setView() {
		sp = getShared();
		mBtnLogOut = (Button) findViewById(R.id.btnLogOut);

		mLinearMentricType = (LinearLayout) findViewById(R.id.linearMetricType);
		mLinearCountDount = (LinearLayout) findViewById(R.id.linearCountDount);
		// chBoxAutoPause = (CheckBox) findViewById(R.id.checkBoxAutoPause);
		mTxtMetricType = (TextView) findViewById(R.id.txtMetric);
		mTxtCountDount = (TextView) findViewById(R.id.txtCountDount);
		mEditFullName = (EditText) findViewById(R.id.edTextFullName);
		mTxtEmail = (TextView) findViewById(R.id.txtEmail);
		mTxtWeight = (TextView) findViewById(R.id.txtWeight);
		mTxtLocation = (TextView) findViewById(R.id.txtLocation);
		mTxtAudioTiming = (TextView) findViewById(R.id.txtAudioTiming);
		mImageView = (ImageView) findViewById(R.id.imVTest);
		if (sp.getInt(Constants.INTENT_KEY.COUNTDOUNT, 0) == 0) {
			sp.edit().putInt(Constants.INTENT_KEY.COUNTDOUNT, deffCountdown)
					.commit();
		}
		setAudioTimming();
		mTxtMetricType.setText(sp.getString(Constants.INTENT_KEY.METRIC,
				getResources().getString(R.string.btn_miles)));

		mTxtCountDount
				.setText(CommonHelper.getCountdownText(sp.getInt(
						Constants.INTENT_KEY.COUNTDOUNT, deffCountdown), this));

		mLinearCountDount.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				startActivityForResult(new Intent(ActivitySetup.this,
						ActivityCountDount.class),
						Constants.REQUEST_CODE_COUNTDOUNT);
				startTransferAnim();
			}
		});
		findViewById(R.id.linearAudioTiming).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(ActivitySetup.this,
								ActivityAudioTiming.class),
								Constants.REQUEST_CODE_AUDIO_TIMING);
						startTransferAnim();
					}
				});
		findViewById(R.id.layoutProfilePicture).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AnalyticsUtils.sendPageViews(ActivitySetup.this,

						AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SETUP,
								AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
								AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
								"profile picture", 0);
						selectAvatarMode();
					}
				});
		findViewById(R.id.layoutLocation).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(ActivitySetup.this,
								ActivityLocation.class),
								Constants.REQUEST_CODE_TRANSFER);
						startTransferAnim();
					}
				});
		findViewById(R.id.layoutWeight).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						double weightExtra;
						if (weight == 0) {
							weightExtra = deffWeight;
						} else {
							weightExtra = weight;
						}
						startActivityForResult(
								new Intent(ActivitySetup.this,
										ActivitySignupDetailsWeight.class)
										.putExtra(Constants.INTENT_KEY.WEIGHT,
												weightExtra).putExtra(
												Constants.INTENT_KEY.UNIT,
												weightUnits),
								Constants.REQUEST_CODE_FEMALE);

					}
				});
		mLinearMentricType.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {

				startActivityForResult(new Intent(ActivitySetup.this,
						ActivityMetric.class), Constants.REQUEST_CODE_METRIC);

			}
		});
		findViewById(R.id.linearSupport).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View paramView) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse(Constants.URL_FEEDBACK));
						startActivity(browserIntent);

					}
				});
		// chBoxAutoPause.setChecked(getShared().getBoolean(
		// Constants.SharedPreferencesKeys.AUTO_PAUSE, false));

		// chBoxAutoPause
		// .setOnCheckedChangeListener(new OnCheckedChangeListener() {
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		//
		// AnalyticsUtils.sendPageViews(ActivitySetup.this,
		//
		// AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SETUP,
		// AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
		// AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
		// "autopause=" + String.valueOf(isChecked), 0);
		// getShared()
		// .edit()
		// .putBoolean(
		// Constants.SharedPreferencesKeys.AUTO_PAUSE,
		// isChecked).commit();
		//
		// }
		// });
		mBtnLogOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				AnalyticsUtils.sendPageViews(ActivitySetup.this,

				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SETUP,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "LogOut", 0);
				logout(ActivitySetup.this);
			}
		});

		if (currentUser.getProfileImage225url().startsWith("http")) {
			imageLoader.displayImage(currentUser.getProfileImage225url(),
					mImageView, options);
		} else {
			mImageView.setImageResource(R.drawable.avatar);
		}
		weightUnits = profileUser.getWeightUnit();
		weight = profileUser.getWeight();

		mEditFullName
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (actionId == EditorInfo.IME_ACTION_DONE) {
							hideKeyboard(ActivitySetup.this);

							String[] firstName = mEditFullName.getText()
									.toString().split(" ");
							if (firstName.length == countOfFullName
									&& (!firstName[0]
											.equalsIgnoreCase(currentUser
													.getFirstName()) || !firstName[1]
											.equalsIgnoreCase(currentUser
													.getLastName()))) {
								AnalyticsUtils.sendPageViews(
										ActivitySetup.this, "SetupScreen",
										"Edit", "field", "fullName", 0);
								jsonObjSend = new JSONObject();
								try {

									jsonObjSend.put(FIRST_NAME, firstName[0]);
									jsonObjSend.put(LAST_NAME, firstName[1]);

								} catch (JSONException e) {

								}

								currentUser.setFirstName(firstName[0]);
								currentUser.setLastName(firstName[1]);
								baseBl.createOrUpdate(User.class, currentUser);
								updateUser(updateUser);

							} else {
								Toast.makeText(
										ActivitySetup.this,
										getResources()
												.getString(
														R.string.toast_sign_up_lastname),
										Toast.LENGTH_SHORT).show();
							}
							return true;
						}

						return false;
					}
				});

	}

	private void setData() {
		profileUser = baseBl.getFromDBByField(ProfileUser.class,
				ProfileUser.ID, currentUser.getProfileID());
		mEditFullName.setText(currentUser.getFirstName() + " "
				+ currentUser.getLastName());
		mTxtEmail.setText(currentUser.getEmail());
		if (profileUser != null) {
			mTxtWeight.setText(profileUser.getWeight() + " "
					+ profileUser.getWeightUnit());
			mTxtLocation.setText(profileUser.getLocationName());
		}else{
			profileUser=new ProfileUser();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.REQUEST_CODE_METRIC) {
			mMetricType = data.getStringExtra(Constants.INTENT_KEY.METRIC);
			mTxtMetricType.setText(mMetricType);
			sp.edit().putString(Constants.INTENT_KEY.METRIC, mMetricType)
					.commit();

			setAudioTimming();
		} else {
			if (resultCode == Constants.REQUEST_CODE_COUNTDOUNT) {
				mCountDown = data.getIntExtra(Constants.INTENT_KEY.COUNTDOUNT,
						deffCountdown);
				mTxtCountDount.setText(data
						.getStringExtra(Constants.INTENT_KEY.UNIT));

				sp.edit().putInt(Constants.INTENT_KEY.COUNTDOUNT, mCountDown)
						.commit();
			}
			if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
				handleBigCameraPhoto();
				uploadPhoto();
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
					uploadPhoto();
				}
			}
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
				mTxtWeight.setText(buffer);

				jsonObjSend = new JSONObject();
				try {

					jsonObjSend.put(WEIGHT, String.valueOf(weight));
					jsonObjSend.put(WEIGHT_UNIT, weightUnits);

				} catch (JSONException e) {

					e.printStackTrace();
				}
				profileUser.setWeight(weight);
				profileUser.setWeightUnit(weightUnits);
				baseBl.createOrUpdate(ProfileUser.class, profileUser);
				currentUser.setProfileUser(profileUser);
				baseBl.createOrUpdate(User.class, currentUser);
				updateUser(null);

			}
			if (resultCode == Constants.REQUEST_CODE_AUDIO_TIMING) {

				mAudioTiming = data.getFloatExtra(
						Constants.INTENT_KEY.AUDIO_TIMING_METRIC, 0);
				if (mAudioTiming == -1) {
					mTxtAudioTiming.setText(getString(R.string.never));
				} else {
					mTxtAudioTiming.setText(getString(R.string.every)
							+ " "
							+ new DecimalFormat("0.#").format(mAudioTiming)
									.replace(",", ".").replace("0", "")
							+ " "
							+ (isMetric() ? getResources().getString(
									R.string.kilometer) : getResources()
									.getString(R.string.mi)));
				}
				if (isMetric()) {
					sp.edit()
							.putFloat(Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
									(float) mAudioTiming).commit();
				} else {
					sp.edit()
							.putFloat(Constants.INTENT_KEY.AUDIO_TIMING_MILE,
									(float) mAudioTiming).commit();
				}
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
						meTask();
					}
				} catch (Exception e) {

				}
			}
		};
		SendPhotoTask sendPhotoTask = new SendPhotoTask(ActivitySetup.this,
				getURLHost(), getPublicKey(), getPrivateKey(), file,
				getApikey(), getUserName());
		getTaskManager().executeTask(sendPhotoTask, upload, null, true);
	}

	protected void meTask() {
		final OnTskCpltListener meTask = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<User> result;
				try {

					result = (TaskResult<User>) task.get();
					if (result.isError()) {
						Toast.makeText(ActivitySetup.this,
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
						currentUser = result.getResult();
						setData();
					}
				} catch (Exception e) {
				}
			}

		};
		GetMeTask getMeTask = new GetMeTask(ActivitySetup.this, getURLHost(),
				getPublicKey(), getPrivateKey(), getUserName(), getApikey(),
				baseBl);
		getTaskManager().executeTask(getMeTask, meTask, null, true);
	}
}
