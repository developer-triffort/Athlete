package com.athlete.activity.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.util.AnalyticsUtils;

public class ActivitySignupDetailsGender extends BaseActivity {
	/**
	 * @author edBaev
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_signup_details_gender);
		setView();
		startTransferAnim();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySignupDetailsGender.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_GENDER);
	}

	private void setView() {
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();

			}
		});
		findViewById(R.id.btnFemale).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						setResult(Constants.REQUEST_CODE_FEMALE);
						finish();
						startTransferAnim();
					}
				});
		findViewById(R.id.btnMale).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						setResult(Constants.REQUEST_CODE_MALE);
						finish();
						startTransferAnim();
					}
				});
	}
}
