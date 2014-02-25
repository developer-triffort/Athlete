package com.athlete.activity.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SignUpTask;
import com.athlete.util.AnalyticsUtils;

public class ActivitySignup extends ActivityBaseAuth implements
		OnTskCpltListener {
	/**
	 * @author edBaev
	 */
	private EditText mEdTxtEmail, mEdTxtPass, mEdTxtFullName;
	private Button mBtnSignUp, mBtnSignUpWithFB;
	private TextView mTxtTerms;
	private final int countOfFullName = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnalyticsUtils.sendPageViews(ActivitySignup.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SIGNUP);
		setContentView(R.layout.actv_signup);
		setView();
	}

	private void setView() {
		mEdTxtEmail = (EditText) findViewById(R.id.edTxtEmail);
		mEdTxtPass = (EditText) findViewById(R.id.edTxtPass);
		mEdTxtFullName = (EditText) findViewById(R.id.edTxtFullName);
		mTxtTerms = (TextView) findViewById(R.id.txtTerms);
		mTxtTerms
				.setText(Html
						.fromHtml("<font color='#FFFFFF'>By signing up, I accept</font><font color='#54A1C7'> Terms of Use</font>"));
		mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
		mBtnSignUpWithFB = (Button) findViewById(R.id.btnFB);
		mBtnSignUpWithFB.setText(Html
				.fromHtml("<b>Sign up </b>with <b>facebook</b>"));
		mBtnSignUp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] firstName = mEdTxtFullName.getText().toString()
						.split(" ");
				if (firstName.length == countOfFullName) {
					SignUpTask task = new SignUpTask(ActivitySignup.this,
							getURLHost(), getPublicKey(), getPrivateKey(),
							mEdTxtEmail.getText().toString(), firstName[0],
							firstName[1], mEdTxtPass.getText().toString());
					getTaskManager().executeTask(task, ActivitySignup.this,
							null, true);
					AnalyticsUtils.sendPageViews(ActivitySignup.this,
							AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SIGNUP,
							AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
							AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
							AnalyticsUtils.GOOGLE_ANALYTICS.SIGNUP_EMAIL, 0);
				} else {
					Toast.makeText(
							ActivitySignup.this,
							getResources().getString(
									R.string.toast_sign_up_lastname),
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		mBtnSignUpWithFB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFBClick = true;
				mIsLogin = false;
				isSaveTrack = false;
				AnalyticsUtils.sendPageViews(ActivitySignup.this,
						AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SIGNUP,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
						AnalyticsUtils.GOOGLE_ANALYTICS.SIGNUP_FB, 0);
				mFacebook.authorize(ActivitySignup.this, Constants.PERMS,
						new LoginDialogListener());

			}
		});
		mTxtTerms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ActivitySignup.this,
						ActivitySignupDetailsTermsOfUse.class));
				startTransferAnim();
			}
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onTaskComplete(BaseTask task) {
		TaskResult<String[]> result;
		try {
			result = (TaskResult<String[]>) task.get();
			if (result.isError()) {
				String toast;
				if (result.getError_description() != null
						&& result.getError_description().length() > 0) {
					toast = result.getError_description();
				} else {
					toast = getString(R.string.toast_non_internet);
				}
				Toast.makeText(ActivitySignup.this, toast, Toast.LENGTH_SHORT)
						.show();
			} else {

				SharedPreferences sp = getSharedPreferences(
						Constants.PREFERENCES, Context.MODE_PRIVATE);
				sp.edit()
						.putString(Constants.SharedPreferencesKeys.USER_NAME,
								result.getResult()[0]).commit();
				sp.edit()
						.putString(Constants.SharedPreferencesKeys.API_KEY,
								result.getResult()[1]).commit();

				meTask(result.getResult()[0], result.getResult()[1]);

			}

		} catch (Exception e) {

		}
	}
}
