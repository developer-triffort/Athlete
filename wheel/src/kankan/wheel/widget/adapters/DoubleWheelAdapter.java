/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget.adapters;

import java.text.DecimalFormat;

import android.content.Context;

/**
 * Numeric Wheel adapter.
 */
public class DoubleWheelAdapter extends AbstractWheelTextAdapter {

	/** The default min value */
	public static final double DEFAULT_MAX_VALUE = 9;

	/** The default max value */
	private static final double DEFAULT_MIN_VALUE = 0;
	/** The default step value */
	private static final double DEFAULT_STEP_VALUE = 0.1;
	// Values
	private double minValue;
	private double maxValue;
	private double step;
	private boolean startFromSecondChar;
	// format
	private String format = "%1$.##";

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 */
	public DoubleWheelAdapter(Context context) {
		this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE, DEFAULT_STEP_VALUE,
				false);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 */
	public DoubleWheelAdapter(Context context, double minValue,
			double maxValue, double step, boolean startFromSecondChar) {
		this(context, minValue, maxValue, null, step, startFromSecondChar);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 * @param format
	 *            the format string
	 */
	public DoubleWheelAdapter(Context context, double minValue,
			double maxValue, String format, double step,
			boolean startFromSecondChar) {
		super(context);

		this.minValue = minValue;
		this.maxValue = maxValue;
		this.step = step;
		this.startFromSecondChar = startFromSecondChar;
	}

	@Override
	public CharSequence getItemText(int index) {
		if (index >= 0 && index < getItemsCount()) {
			double value = minValue + (index * step);
			DecimalFormat f = new DecimalFormat("0.##");
			if (startFromSecondChar)
				f = new DecimalFormat("0.0");
			String str = f.format(value).replace(",", ".");
			if (startFromSecondChar)
				return str.substring(1);
			else
				return str;
		}
		return null;
	}

	@Override
	public int getItemsCount() {
		if (startFromSecondChar)
			return (int) (maxValue / step) + 1;
		else
			return (int) (maxValue / step);
	}
}
