package com.athlete.activity.track;

import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;

public class ActivityTimePicker extends BaseActivity {

	private WheelView hourWeel, minuteWeel, secsWeel;
	private final int minValue = 0;
	private final int maxValueHour = 12;
	private final int maxValueMinSec = 59;
	private final int time60 = 60;
	private final int currentDefMinutes = 40;
	private final int textSize = 16;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 11) {
			setTheme(android.R.style.Theme_Holo_Dialog_MinWidth);
		}
		setContentView(R.layout.act_duration_picker);
		setView();
		transitionType = TransitionType.Zoom;
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
	}

	private void setView() {
		hourWeel = (WheelView) findViewById(R.id.hour);
		TextView txtHour = (TextView) findViewById(R.id.txtPickerHour);
		txtHour.setTag(getString(R.string.lbl_hours));
		TextView txtMinute = (TextView) findViewById(R.id.txtPickerMinute);
		txtMinute.setTag(getString(R.string.lbl_minutes));
		TextView txtSecond = (TextView) findViewById(R.id.txtPickerSecond);
		txtSecond.setTag(getString(R.string.lbl_secs));
		hourWeel.setViewAdapter(new WeightAdapter(this, minValue, maxValueHour,
				0, txtHour));
		hourWeel.setCurrentItem(0);

		minuteWeel = (WheelView) findViewById(R.id.minute);
		minuteWeel.setCyclic(true);

		minuteWeel.setViewAdapter(new WeightAdapter(this, minValue,
				maxValueMinSec, 0, txtMinute));
		minuteWeel.setCurrentItem(currentDefMinutes);
		secsWeel = (WheelView) findViewById(R.id.second);
		secsWeel.setCyclic(true);

		secsWeel.setViewAdapter(new WeightAdapter(this, minValue,
				maxValueMinSec, 0, txtSecond));
		secsWeel.setCurrentItem(0);

		OnWheelClickedListener click = new OnWheelClickedListener() {
			public void onItemClicked(WheelView wheel, int itemIndex) {
				wheel.setCurrentItem(itemIndex, true);
			}
		};
		hourWeel.addClickingListener(click);
		minuteWeel.addClickingListener(click);
		secsWeel.addClickingListener(click);
		findViewById(R.id.btnSelect).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						long time = (hourWeel.getCurrentItem() * time60 * time60)
								+ (minuteWeel.getCurrentItem() * time60)
								+ secsWeel.getCurrentItem();
						Intent data = new Intent();
						data.putExtra(Constants.INTENT_KEY.TIME, time);

						setResult(Constants.REQUEST_CODE_DURATION, data);
						finish();
						transitionType = TransitionType.Zoom;
						overridePendingTransition(R.anim.zoom_in,
								R.anim.zoom_out);
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
				int current, TextView txtLabel) {
			super(context, minValue, maxValue);
			this.currentValue = current;

			setTextSize(textSize);
			setTextGravity(Gravity.LEFT);
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
