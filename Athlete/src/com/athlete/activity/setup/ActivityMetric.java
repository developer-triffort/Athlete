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

public class ActivityMetric extends BaseSetupActivity {
	/**
	 * @author edBaev
	 */
	private SharedPreferences sp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startTransferAnim();
		setContentView(R.layout.actv_setup_metric);
		setView();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		startTransferAnim();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityMetric.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_METRIC);
	}

	private void setView() {
		sp = getShared();
		String type = sp.getString(Constants.INTENT_KEY.METRIC, getResources()
				.getString(R.string.btn_miles));

		Button btnMiles = (Button) findViewById(R.id.btnMiles);
		Button btnKilometers = (Button) findViewById(R.id.btnKilometers);

		if (type.equals(getResources().getString(R.string.btn_miles))) {
			btnMiles.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btnMiles.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(Constants.REQUEST_CODE_METRIC, new Intent().putExtra(
						Constants.INTENT_KEY.METRIC,
						getResources().getString(R.string.btn_miles)));
				AnalyticsUtils.sendPageViews(ActivityMetric.this,
						AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_METRIC,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION, "miles", 0);
				finish();
				startTransferAnim();
			}
		});
		if (type.equals(getResources().getString(R.string.btn_kilometers))) {
			btnKilometers.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		}
		btnKilometers.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				setResult(Constants.REQUEST_CODE_METRIC, new Intent().putExtra(
						Constants.INTENT_KEY.METRIC,
						getResources().getString(R.string.btn_kilometers)));
				AnalyticsUtils
						.sendPageViews(
								ActivityMetric.this,
								AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_METRIC,
								AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
								AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
								"kilometers", 0);
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
