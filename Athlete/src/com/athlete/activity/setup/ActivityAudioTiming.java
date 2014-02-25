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

public class ActivityAudioTiming extends BaseSetupActivity {
	/**
	 * @author edBaev
	 */
	private SharedPreferences sp;
	private float mAudioTiming;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startTransferAnim();
		setContentView(R.layout.actv_setup_audio_timing);
		setView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityAudioTiming.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_AUDIO_TIMING);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		startTransferAnim();

	}

	private void setView() {
		sp = getShared();
		if (isMetric()) {
			mAudioTiming = sp.getFloat(
					Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
					Constants.AUDIO_TIMING_FLOAT.ONE);
		} else {
			mAudioTiming = sp.getFloat(Constants.INTENT_KEY.AUDIO_TIMING_MILE,
					Constants.AUDIO_TIMING_FLOAT.ZERO_POINT_FIVE);
		}
		final Button btnNever = (Button) findViewById(R.id.btnNever);
		final Button btnFirst = (Button) findViewById(R.id.btnFirst);
		final Button btnSecond = (Button) findViewById(R.id.btnSecond);

		btnFirst.setText(isMetric() ? getResources()
				.getString(R.string.every_1)
				+ " "
				+ getResources().getString(R.string.kilometer) : getResources()
				.getString(R.string.every_0_5)
				+ " "
				+ getResources().getString(R.string.mi));

		btnSecond.setText(isMetric() ? getResources().getString(
				R.string.every_2_5)
				+ " " + getResources().getString(R.string.kilometer)
				: getResources().getString(R.string.every_1) + " "
						+ getResources().getString(R.string.mi));

		if (mAudioTiming == Constants.AUDIO_TIMING_FLOAT.NEVER) {
			btnNever.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.v), null);
		} else {
			if ((isMetric() && mAudioTiming == Constants.AUDIO_TIMING_FLOAT.ONE)
					|| (!isMetric() && mAudioTiming == Constants.AUDIO_TIMING_FLOAT.ZERO_POINT_FIVE)) {
				btnFirst.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.drawable.v), null);
			} else {
				btnSecond.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.drawable.v), null);
			}
		}
		btnFirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_AUDIO_TIMING,
						new Intent()
								.putExtra(
										Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
										isMetric() ? Constants.AUDIO_TIMING_FLOAT.ONE
												: Constants.AUDIO_TIMING_FLOAT.ZERO_POINT_FIVE));
				if (isMetric()) {
					AnalyticsUtils
							.sendPageViews(
									ActivityAudioTiming.this,
									AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_AUDIO_TIMING,
									AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
									AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
									"1 km", 0);
				} else {
					AnalyticsUtils
							.sendPageViews(
									ActivityAudioTiming.this,
									AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_AUDIO_TIMING,
									AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
									AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
									"0.5 mi", 0);
				}
				finish();
				startTransferAnim();
			}
		});
		btnNever.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(Constants.REQUEST_CODE_AUDIO_TIMING, new Intent()
						.putExtra(Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
								Constants.AUDIO_TIMING_FLOAT.NEVER));

				AnalyticsUtils
						.sendPageViews(
								ActivityAudioTiming.this,
								AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_AUDIO_TIMING,
								AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
								AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
								"Never", 0);

				finish();
				startTransferAnim();

			}
		});
		btnSecond.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				setResult(
						Constants.REQUEST_CODE_AUDIO_TIMING,
						new Intent()
								.putExtra(
										Constants.INTENT_KEY.AUDIO_TIMING_METRIC,
										isMetric() ? Constants.AUDIO_TIMING_FLOAT.TWO_POINT_FIVE
												: Constants.AUDIO_TIMING_FLOAT.ONE));
				if (isMetric()) {
					AnalyticsUtils
							.sendPageViews(
									ActivityAudioTiming.this,
									AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_AUDIO_TIMING,
									AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
									AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
									"2.5 km", 0);
				} else {
					AnalyticsUtils
							.sendPageViews(
									ActivityAudioTiming.this,
									AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_AUDIO_TIMING,
									AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
									AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
									"1 mi", 0);
				}
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
