/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.athlete.google.android.apps.mytracks.util;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.athlete.R;
import com.athlete.google.android.apps.mytracks.stats.TripStatistics;
import com.athlete.util.CommonHelper;

/**
 * Utilities for updating the statistics UI labels and values.
 * 
 * @author Jimmy Shih
 */
public class StatsUtils {

	private StatsUtils() {
	}

	/**
	 * Sets the total time value.
	 * 
	 * @param activity
	 *            the activity
	 * @param totalTime
	 *            the total time
	 */

	public static void setTotalTimeValue(Activity activity, long totalTime) {
		setTimeValue(activity, R.id.txtTime, totalTime);
	}

	/**
	 * Sets the trip statistics values.
	 * 
	 * @param activity
	 *            the activity
	 * @param tripStatistics
	 *            the trip statistics
	 */
	public static void setTripStatisticsValues(Activity activity,
			TripStatistics tripStatistics, double weight) {
		// boolean metricUnits = PreferencesUtils.getMetricUnit(activity);
		boolean metricUnits = PreferencesUtils.getBoolean(activity,
				R.string.metric_units_key,
				PreferencesUtils.METRIC_UNITS_DEFAULT);
		// Set total distance
		double totalDistance = tripStatistics == null ? Double.NaN
				: tripStatistics.getTotalDistance();
		setDistanceValue(activity, R.id.txtDistanse, R.id.txtUnit,
				totalDistance, metricUnits);

		double averageMovingSpeed = tripStatistics != null ? tripStatistics
				.getAverageMovingSpeed() : Double.NaN;
		setSpeedValue(activity, R.id.txtAvgPlace, averageMovingSpeed,
				metricUnits, false);
		if (metricUnits) {
			((TextView) activity.findViewById(R.id.txtClimbLabel))
					.setText(activity.getString(R.string.meter) + " "
							+ activity.getString(R.string.climb));
		} else {
			((TextView) activity.findViewById(R.id.txtClimbLabel))
					.setText(activity.getString(R.string.ft) + " "
							+ activity.getString(R.string.climb));
		}
		long movingTime = tripStatistics != null ? tripStatistics
				.getMovingTime() : 0;
		if (weight > 0) {
			((TextView) activity.findViewById(R.id.txtCalories)).setText(String
					.valueOf(CommonHelper.calculateCalories(weight,
							movingTime / 1000, (float) totalDistance)));
		}
		double elevationGain = tripStatistics == null ? Double.NaN
				: tripStatistics.getTotalElevationGain();

		setElevationValue(activity, R.id.txtClimb, elevationGain, metricUnits);

	}

	/**
	 * Sets a speed value.
	 * 
	 * @param activity
	 *            the activity
	 * @param id
	 *            the speed value resource id
	 * @param speed
	 *            the speed in meters per second
	 * @param metricUnits
	 *            true to display in metric units
	 * @param reportSpeed
	 *            true to report speed
	 */
	public static void setSpeedValue(Activity activity, int id, double speed,
			boolean metricUnits, boolean reportSpeed) {
		TextView textView = (TextView) activity.findViewById(id);
		if (textView == null) {
			return;
		}
		textView.setText(StringUtils.formatSpeed(activity, speed, metricUnits,
				reportSpeed));
	}

	/**
	 * Sets a distance value.
	 * 
	 * @param activity
	 *            the activity
	 * @param id
	 *            the distance value resource id
	 * @param distance
	 *            the distance in meters
	 * @param metricUnits
	 *            true to display in metric units
	 */
	public static void setDistanceValue(Activity activity, int idValue,
			int idType, double distance, boolean metricUnits) {
		TextView textViewValue = (TextView) activity.findViewById(idValue);
		TextView textViewType = (TextView) activity.findViewById(idType);
		if (textViewValue == null) {
			return;
		}
		String value = StringUtils.formatDistance(activity, distance,
				metricUnits, true).first;
		if (value.equals("-"))
			value = "0.00";
		String valuesArr[] = value.split("\\.");
		StringBuffer buffer = new StringBuffer();
		if (valuesArr.length > 1) {
			if (valuesArr[0].equalsIgnoreCase("0")) {
				buffer.append(valuesArr[0]);

				buffer.append("<font color='#000000'>." + valuesArr[1]
						+ "</font>");
			} else {
				textViewValue.setTextColor(Color.BLACK);
				buffer.append(value);
			}
		} else {
			buffer.append(value);
		}
		textViewValue.setText(Html.fromHtml(buffer.toString()));
		if (textViewType != null) {
			textViewType.setText(StringUtils.formatDistance(activity, distance,
					metricUnits, true).second);
		}
	}

	/**
	 * Sets a time value.
	 * 
	 * @param activity
	 *            the activity
	 * @param id
	 *            the time value resource id
	 * @param time
	 *            the time
	 */
	private static void setTimeValue(Activity activity, int id, long time) {
		TextView textView = (TextView) activity.findViewById(id);
		if (textView == null) {
			return;
		}
		String value = time == -1L ? activity.getString(R.string.value_unknown)
				: StringUtils.formatElapsedTime(time);

		textView.setText(value);
	}

	/**
	 * Sets an elevation value.
	 * 
	 * @param activity
	 *            the activity
	 * @param id
	 *            the elevation value resource id
	 * @param elevation
	 *            the elevation in meters
	 * @param metricUnits
	 *            true to display in metric units
	 */
	public static void setElevationValue(Activity activity, int id,
			double elevation, boolean metricUnits) {

		final String lang = PreferenceManager.getDefaultSharedPreferences(
				activity).getString("locale", "en");
		final Locale newLocale = new Locale(lang);
		Locale.setDefault(newLocale);
		final Configuration config = new Configuration();
		config.locale = newLocale;

		final Resources res = activity.getResources();
		res.updateConfiguration(config, res.getDisplayMetrics());

		TextView textView = (TextView) activity.findViewById(id);
		if (textView == null) {
			return;
		}
		String value;
		if (Double.isNaN(elevation) || Double.isInfinite(elevation)) {
			value = res.getString(R.string.value_unknown);
		} else {
			if (metricUnits) {
				value = res.getString(R.string.value_float_meter, elevation)
						.replace(",", ".");
			} else {
				elevation *= UnitConversions.M_TO_FT;
				value = res.getString(R.string.value_float_feet, elevation)
						.replace(",", ".");
			}
		}
		textView.setText(value);
	}

	private static void setCoordinateValue(Activity activity, int id,double coordinate) {
		TextView textView = (TextView) activity.findViewById(id);
		if (textView == null) {
			return;
		}
		String value;
		if (Double.isNaN(coordinate) || Double.isInfinite(coordinate)) {
			value = activity.getString(R.string.value_unknown);
		} else {
			value = activity.getString(R.string.value_coordinate_degree,
					Location.convert(coordinate, Location.FORMAT_DEGREES));
		}
		textView.setText(value);
	}

}
