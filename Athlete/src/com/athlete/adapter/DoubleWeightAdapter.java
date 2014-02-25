package com.athlete.adapter;

import kankan.wheel.widget.adapters.DoubleWheelAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DoubleWeightAdapter extends DoubleWheelAdapter {
	// Index of current item
	int currentItem;
	// Index of item to be highlighted
	int currentValue;
	private final int textSize = 22;

	/**
	 * Constructor
	 */
	public DoubleWeightAdapter(Context context, double minValue,
			double maxValue, int current, double stepAdapter,
			boolean startFromSecondChar) {
		super(context, minValue, maxValue, stepAdapter, startFromSecondChar);
		this.currentValue = current;

		setTextSize(textSize);
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