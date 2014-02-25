package com.athlete.activity.auth;

import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.DoubleWeightAdapter;
import com.athlete.util.AnalyticsUtils;

public class ActivitySignupDetailsWeight extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private WheelView weightUnit, weight, weightFractional;
	private int minWeight = 40, maxWeight = 450;
	private int currentDeff = 150;
	private double minDistance = 0.0;
	private double maxDistance = 0.9;
	private double stepAdapter = 0.1;

	private int currentWeightUnit = 0;
	private final int textSizeElementOfPicker = 22;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_signup_details_weight);
		setView();
		startTransferAnim();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySignupDetailsWeight.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_WEIGHT);
	}

	private void setView() {
		weightUnit = (WheelView) findViewById(R.id.weightUnit);

		String utits[] = new String[] { getString(R.string.weight_lbs),
				getString(R.string.weight_kg) };
		String strUnit = getIntent().getStringExtra(Constants.INTENT_KEY.UNIT);
		if (strUnit != null
				&& strUnit.equalsIgnoreCase(Constants.WEIGHT_UNIT.KILOGRAM)) {
			currentWeightUnit = 1;
		}
		weightUnit.setViewAdapter(new WeightUnitArrayAdapter(this, utits,
				currentWeightUnit));
		weightUnit.setCurrentItem(currentWeightUnit);
		double weightDouble = getIntent().getDoubleExtra(
				Constants.INTENT_KEY.WEIGHT, currentDeff);

		currentDeff = (int) weightDouble;
		int currentItemFractional = (int) Math
				.round(((weightDouble - currentDeff) * 10));
		weightFractional = (WheelView) findViewById(R.id.weightFractional);
		weightFractional.setViewAdapter(new DoubleWeightAdapter(this,
				minDistance, maxDistance, currentItemFractional, stepAdapter,
				true));
		weightFractional.setCyclic(true);
		weightFractional.setCurrentItem(currentItemFractional);

		weight = (WheelView) findViewById(R.id.weight);
		weight.setCyclic(true);

		weight.setViewAdapter(new WeightAdapter(this, minWeight, maxWeight,
				currentDeff - minWeight));
		weight.setCurrentItem(currentDeff - minWeight);

		OnWheelClickedListener click = new OnWheelClickedListener() {
			public void onItemClicked(WheelView wheel, int itemIndex) {
				wheel.setCurrentItem(itemIndex, true);
			}
		};
		weightUnit.addClickingListener(click);
		weight.addClickingListener(click);
		weightFractional.addClickingListener(click);
		findViewById(R.id.btnSelect).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String stringWeightUnit;
						if (weightUnit.getCurrentItem() == 0) {
							stringWeightUnit = Constants.WEIGHT_UNIT.POUNDS;
						} else {
							stringWeightUnit = Constants.WEIGHT_UNIT.KILOGRAM;
						}
						Intent data = new Intent();
						data.putExtra(Constants.INTENT_KEY.WEIGHT, String
								.valueOf((weight.getCurrentItem() + minWeight)
										+ (weightFractional.getCurrentItem()
												* stepAdapter + minDistance)));
						data.putExtra(Constants.INTENT_KEY.UNIT,
								stringWeightUnit);
						setResult(Constants.REQUEST_CODE_WEIGHT, data);
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

	private class WeightAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public WeightAdapter(Context context, int minValue, int maxValue,
				int current) {
			super(context, minValue, maxValue);
			this.currentValue = current;

			setTextSize(textSizeElementOfPicker);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				view.setTextColor(Color.parseColor("#54A1C7"));
			} else {
				view.setTextColor(Color.WHITE);
			}
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
	}

	private class WeightUnitArrayAdapter extends ArrayWheelAdapter<String> {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public WeightUnitArrayAdapter(Context context, String[] items,
				int current) {
			super(context, items);
			this.currentValue = current;

			setTextSize(textSizeElementOfPicker);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				view.setTextColor(Color.parseColor("#54A1C7"));
			} else {
				view.setTextColor(Color.WHITE);
			}
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
	}
}
