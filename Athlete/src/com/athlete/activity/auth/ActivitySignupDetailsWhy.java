package com.athlete.activity.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.util.AnalyticsUtils;

public class ActivitySignupDetailsWhy extends BaseActivity {
	/**
	 * @author edBaev
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_signup_details_why);
		startTransferAnim();
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySignupDetailsWhy.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.WHY);
	}
}
