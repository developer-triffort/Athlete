package com.athlete.activity.setup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.util.AnalyticsUtils;

public class ActivityCountDount extends BaseSetupActivity {
	/**
	 * @author edBaev
	 */
	private SharedPreferences sp;
	private final int typeZero = 0;
	private final int typeCountDount10 = 10;
	private final int typeCountDount30 = 30;
	private final int typeCountDount5 = 5;
	private final int typeCountDount3 = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startTransferAnim();
		setContentView(R.layout.actv_setup_count_dount);
		setView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityCountDount.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_COUNTDOWN);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		startTransferAnim();
	}

	private void setView() {
		
		sp = getShared();
		int type = sp.getInt(Constants.INTENT_KEY.COUNTDOUNT, typeZero);
		Button btnNone = (Button) findViewById(R.id.btnNone);
		Button btn3Secconds = (Button) findViewById(R.id.btn3Secconds);
		Button btn5Secconds = (Button) findViewById(R.id.btn5Secconds);
		Button btn10Secconds = (Button) findViewById(R.id.btn10Secconds);
		Button btn30Secconds = (Button) findViewById(R.id.btn30Secconds);

		if (type == typeZero) {

			btnNone.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btnNone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_COUNTDOUNT,
						new Intent().putExtra(Constants.INTENT_KEY.COUNTDOUNT,
								typeZero).putExtra(Constants.INTENT_KEY.UNIT,
								getString(R.string.btn_none)));

				AnalyticsUtils.sendPageViews(ActivityCountDount.this,

				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_COUNTDOWN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "none", 0);

				finish();
				startTransferAnim();
			}
		});

		if (type == typeCountDount3) {

			btn3Secconds.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btn3Secconds.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_COUNTDOUNT,
						new Intent().putExtra(Constants.INTENT_KEY.COUNTDOUNT,
								typeCountDount3).putExtra(
								Constants.INTENT_KEY.UNIT,
								getString(R.string.btn_3_seconds)));
				AnalyticsUtils.sendPageViews(ActivityCountDount.this,

				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_COUNTDOWN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "3 secs", 0);
				finish();
				startTransferAnim();
			}
		});

		if (type == typeCountDount5) {

			btn5Secconds.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btn5Secconds.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_COUNTDOUNT,
						new Intent().putExtra(Constants.INTENT_KEY.COUNTDOUNT,
								typeCountDount5).putExtra(
								Constants.INTENT_KEY.UNIT,
								getString(R.string.btn_5_seconds)));
				AnalyticsUtils.sendPageViews(ActivityCountDount.this,

				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_COUNTDOWN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "5 secs", 0);
				finish();
				startTransferAnim();
			}
		});

		if (type == typeCountDount10) {
			btn10Secconds.setEnabled(false);
			btn10Secconds.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btn10Secconds.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_COUNTDOUNT,
						new Intent().putExtra(Constants.INTENT_KEY.COUNTDOUNT,
								typeCountDount10).putExtra(
								Constants.INTENT_KEY.UNIT,
								getString(R.string.btn_10_seconds)));
				AnalyticsUtils.sendPageViews(ActivityCountDount.this,

				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_COUNTDOWN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "10 secs", 0);
				finish();
				startTransferAnim();
			}
		});

		if (type == typeCountDount30) {
			btn30Secconds.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btn30Secconds.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_COUNTDOUNT,
						new Intent().putExtra(Constants.INTENT_KEY.COUNTDOUNT,
								typeCountDount30).putExtra(
								Constants.INTENT_KEY.UNIT,
								getString(R.string.btn_30_seconds)));
				AnalyticsUtils.sendPageViews(ActivityCountDount.this,

				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_COUNTDOWN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "30 secs", 0);
				finish();
				startTransferAnim();
			}
		});
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();

			}
		});
	}
}
