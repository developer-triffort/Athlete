package com.athlete.activity.auth;

import java.util.Calendar;
import java.util.Date;

import kankan.wheel.widget.OnWheelChangedListener;
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
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

public class ActivitySignupDetailsBD extends BaseActivity {
	/**
	 * @author edBaev
	 */

	private Calendar calendar;
	private WheelView month, year, day;
	private int curYear;
	private int countYears = 60;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_signup_details_bd);
		setView();
		transitionType = TransitionType.Zoom;
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);

	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySignupDetailsBD.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_DB);
	}

	private void setView() {
		month = (WheelView) findViewById(R.id.month);

		year = (WheelView) findViewById(R.id.year);
		day = (WheelView) findViewById(R.id.day);
		calendar = Calendar.getInstance();
		curYear = calendar.get(Calendar.YEAR);
		month.setCyclic(true);
		day.setCyclic(true);
		year.setCyclic(true);
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();

			}
		});

		findViewById(R.id.btnSelect).setOnClickListener(
				new View.OnClickListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onClick(View v) {
						Date date = new Date();
						date.setDate(day.getCurrentItem() + 1);
						date.setMonth(month.getCurrentItem());
						date.setYear(year.getCurrentItem()
								+ (curYear - countYears) - 1900);
						Intent data = new Intent();
						data.putExtra(Constants.INTENT_KEY.BD,
								CommonHelper.getDateFormatYYYYMMDDtHHMMSS(date));
						setResult(Constants.REQUEST_CODE_BD, data);
						finish();
						startTransferAnim();
					}
				});

		OnWheelChangedListener listener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				updateDays(year, month, day);
			}
		};
		// month
		int curMonth = calendar.get(Calendar.MONTH);
		final String months[] = new String[] { "January", "February", "March",
				"April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
		month.setViewAdapter(new DateArrayAdapter(this, months, curMonth));
		month.setCurrentItem(0);
		month.addChangingListener(listener);
		// year
		year.setViewAdapter(new DateNumericAdapter(this, curYear - countYears,
				curYear, countYears));
		year.setCurrentItem(curYear - 30);
		year.addChangingListener(listener);
		// day
		updateDays(year, month, day);
		day.setCurrentItem(0);

		OnWheelClickedListener click = new OnWheelClickedListener() {
			public void onItemClicked(WheelView wheel, int itemIndex) {
				wheel.setCurrentItem(itemIndex, true);
			}
		};
		day.addClickingListener(click);
		year.addClickingListener(click);
		month.addClickingListener(click);
	}

	/**
	 * Updates day wheel. Sets max days according to selected month and year
	 */
	private void updateDays(WheelView year, WheelView month, WheelView day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR,
				calendar.get(Calendar.YEAR) + year.getCurrentItem());
		calendar.set(Calendar.MONTH, month.getCurrentItem());

		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		day.setViewAdapter(new DateNumericAdapter(this, 1, maxDays, calendar
				.get(Calendar.DAY_OF_MONTH) - 1));
		int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
		day.setCurrentItem(curDay - 1, true);
	}

	/**
	 * Adapter for numeric wheels. Highlights the current value.
	 */
	private class DateNumericAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateNumericAdapter(Context context, int minValue, int maxValue,
				int current) {
			super(context, minValue, maxValue);
			this.currentValue = current;

			setTextSize(22);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				view.setTextColor(getResources().getColor(R.color.BLUE_TEXT));
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

	/**
	 * Adapter for string based wheel. Highlights the current value.
	 */
	private class DateArrayAdapter extends ArrayWheelAdapter<String> {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		public DateArrayAdapter(Context context, String[] items, int current) {
			super(context, items);
			this.currentValue = current;

			setTextSize(22);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (currentItem == currentValue) {
				view.setTextColor(getResources().getColor(R.color.BLUE_TEXT));
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
