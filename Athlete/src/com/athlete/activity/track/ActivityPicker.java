package com.athlete.activity.track;

import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.DoubleWeightAdapter;

public class ActivityPicker extends BaseActivity {

	private WheelView distanceUnit, distance;
	private final double minDistance = 0.1;
	private final double maxDistance = 40;
	private final double stepAdapter = 0.1;
	private final int currentItem = (int) ((5 / stepAdapter) - 1);
	private final int textSizeForItem = 22;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 11) {
			setTheme(android.R.style.Theme_Holo_Dialog_MinWidth);
		}
		setContentView(R.layout.act_picker);
		setView();
		transitionType = TransitionType.Zoom;
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
	}

	private void setView() {
		distanceUnit = (WheelView) findViewById(R.id.distanceUnit);

		distanceUnit.setViewAdapter(new WeightUnitArrayAdapter(this,
				Constants.UNITS, 0));
		distanceUnit.setCurrentItem(0);

		distance = (WheelView) findViewById(R.id.distance);
		distance.setCyclic(true);

		distance.setViewAdapter(new DoubleWeightAdapter(this, minDistance,
				maxDistance, 0, stepAdapter, false));
		distance.setCurrentItem(currentItem);

		OnWheelClickedListener click = new OnWheelClickedListener() {
			public void onItemClicked(WheelView wheel, int itemIndex) {
				wheel.setCurrentItem(itemIndex, true);
			}
		};
		distanceUnit.addClickingListener(click);
		distance.addClickingListener(click);

		findViewById(R.id.btnSelect).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent data = new Intent();
						data.putExtra(Constants.INTENT_KEY.METRIC,
								distance.getCurrentItem() * stepAdapter
										+ minDistance);
						data.putExtra(Constants.INTENT_KEY.UNIT,
								distanceUnit.getCurrentItem());
						setResult(Constants.REQUEST_CODE_METRIC, data);
						finish();
						transitionType = TransitionType.Zoom;
						overridePendingTransition(R.anim.zoom_in,
								R.anim.zoom_out);

					}
				});

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

			setTextSize(textSizeForItem);
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
