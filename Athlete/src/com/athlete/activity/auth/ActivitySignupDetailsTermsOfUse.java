package com.athlete.activity.auth;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.util.AnalyticsUtils;

public class ActivitySignupDetailsTermsOfUse extends BaseActivity {
	/**
	 * @author edBaev
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_signup_details_terms_of_use);
		startTransferAnim();
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.loadUrl(Constants.URL_TERMS);
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
		AnalyticsUtils.sendPageViews(ActivitySignupDetailsTermsOfUse.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.TERMS);
	}
}
