package com.athlete.activity.track;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;
import com.athlete.model.ActivityType;
import com.athlete.model.IdleWorkOut;
import com.athlete.util.AnalyticsUtils;

public class ActivityManual extends BaseSaveTrackActivity {
	private long distanceInMeters = -1L;

	/**
	 * @author edBaev
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_manual_entry);

        workoutActivityType = ActivityType.getDefaultActivityType();

		findViewById(R.id.linearDistance).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(ActivityManual.this,
								ActivityPicker.class),
								Constants.REQUEST_CODE_METRIC);
						startTransferAnim();
					}
				});
		findViewById(R.id.linearDuration).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(ActivityManual.this,
								ActivityTimePicker.class),
								Constants.REQUEST_CODE_DURATION);
						startTransferAnim();
					}
				});
		name = (EditText) findViewById(R.id.track_edit_name);
		checkFB = (CheckBox) findViewById(R.id.checkBoxFb);

        mTxtActivityType = (TextView) findViewById(R.id.txtActivityType);
        mTxtActivitySubtype = (TextView) findViewById(R.id.txtActivitySubtype);

		mTxtRouteView = (TextView) findViewById(R.id.txtRouteView);
		mTxtTitle = (TextView) findViewById(R.id.txtTitle);
		time = 0L;

		setTitle();
		findViewById(R.id.activityType).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						setAdapterForDialog();
					}
				});
        findViewById(R.id.activitySubType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialogForActivitySubtype();
            }
        });
		findViewById(R.id.btnDisacard).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideKeyboard(ActivityManual.this);
						alertDialog();

					}
				});
		findViewById(R.id.runPrivace).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						setAdapterForDialogPrivacy();
					}
				});
		description = (EditText) findViewById(R.id.track_edit_description);
		Button save = (Button) findViewById(R.id.track_edit_save);
		/*
		 * save the track
		 */
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard(ActivityManual.this);

				if (distanceInMeters > 0 && time != null) {
					AnalyticsUtils.sendPageViews(ActivityManual.this,
							AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.MANUAL,
							AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
							AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "save", 0);
					IdleWorkOut idleWorkOut = new IdleWorkOut();
					idleWorkOut.setDistance(distanceInMeters);
					idleWorkOut.setDuration(time);
					idleWorkOut.setTitle(name.getText().toString());
					idleWorkOut.setPostBody(description.getText().toString());
                    idleWorkOut.setActivityType(mTxtActivityType.getText().toString());
                    idleWorkOut.setActivitySubType(mTxtActivitySubtype.getText().toString());
					idleWorkOut.setPrivacy(mTxtRouteView.getText().toString());
					idleWorkOut.setFbAcces(mFBAcces);
					idleWorkOut.setTrackPath(null);
					idleWorkOut
							.setPhotoPath(((AthleteApplication) getApplication())
									.getPathes());
					idleWorkOut.setIdUser(getUserID());
					baseBl.createOrUpdate(IdleWorkOut.class, idleWorkOut);
					setResult(com.athlete.Constants.RESULT_CODE_TAB);
					finish();
				} else {
					Toast.makeText(ActivityManual.this,
							getString(R.string.toast_1_point),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		/*
		 * turn on/off facebook
		 */
		checkFB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {

					mFBAcces = getShared().getString(
							Constants.SharedPreferencesKeys.FB_ACCES, null);
					if (mFBAcces == null) {
						callFBAuthSavetrack();
					}
				}
			}
		});

	}

    @Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityManual.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.MANUAL);
	}

	private void callFBAuthSavetrack() {

		mFBClick = true;

		isSaveTrack = true;

		mFacebook.authorize(ActivityManual.this, Constants.PERMS,
				new LoginDialogListener());

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.REQUEST_CODE_METRIC) {
			distance = data.getDoubleExtra(Constants.INTENT_KEY.METRIC, 0.0);
			typeID = data.getIntExtra(Constants.INTENT_KEY.UNIT, 0);
			distanceToMeters();
			setTitle();
		}
		if (resultCode == Constants.REQUEST_CODE_DURATION) {
			time = data.getLongExtra(Constants.INTENT_KEY.TIME, -1L);
			setTitle();
		}
	}

	private void distanceToMeters() {
		distanceInMeters = (long) (typeID == 1 ? distance * 1000 : distance
				* UnitConversions.MI_TO_M);
		Log.w("DISNATCE", String.valueOf(distanceInMeters));
	}

	@Override
	public void onBackPressed() {
		alertDialog();
	}

	private void alertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_discard))
				.setMessage(getString(R.string.message_discard))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AnalyticsUtils.sendPageViews(
										ActivityManual.this, "ManualScreen",
										"Clicks", "button", "discard run", 0);
								setResult(com.athlete.Constants.RESULT_CODE_TRACK);
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}

	private void setTitle() {
		StringBuffer buffer = new StringBuffer();
		if (distance != 0) {
			buffer.append(new DecimalFormat("0.##").format(distance).replace(
					",", ".")
					+ " " + Constants.UNITS[typeID]);
		}
		if (distance != 0 && time != 0 && time != -1L) {
			buffer.append(" / ");
		}
		if (time != 0 && time != -1L) {
			buffer.append(DateUtils.formatElapsedTime(time));
		}
		if (buffer.length() != 0)
			mTxtTitle.setText(buffer);
	}

}
