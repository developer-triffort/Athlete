package com.athlete.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.exception.InvalidActivitySubTypeException;
import com.athlete.exception.InvalidActivityTypeException;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;
import com.athlete.model.ActivityType;
import com.athlete.model.WorkOut;

/**
 * @author edBaev
 * */
public class CommonHelper {
	private static double metersToMiles = 0.000621371192d;
	private static double metersToKm = 0.001d;
	// Calories/Hour
	private static int BASE_CAL_PER_HOUR = 700;
	private static int BASE_WEIGHT_LBS = 150;
	private static int BASE_PACE_MPH = 6;
	private static int sdk = android.os.Build.VERSION.SDK_INT;
	private static final int MINUTE = 60;
	private static final long oneSecLong = 1000L;
	private static final int hours24 = 24;
	private static final int hours23 = 23;
	private static final int WEEK = 7;
	private static final int fourDay = 4;
	private static final String algoritm = "HmacSHA256";
	private static final String FAST_FORMAT_HMMSS = "%1$d:%2$02d:%3$02d";
	private static final String FAST_FORMAT_MMSS = "%1$02d:%2$02d";
	private static final char TIME_PADDING = '0';
	private static final String TIME_SEPARATOR = ":";

	public static void dumpString(String longString) {
		if (Constants.DEBUG) {
			Log.v("DUMP_STRING", longString);
		}

	}

	public static String formatElapsedTime(long elapsedSeconds) {
		long hours = 0;
		long minutes = 0;
		long seconds = 0;

		if (elapsedSeconds >= 3600) {
			hours = elapsedSeconds / 3600;
			elapsedSeconds -= hours * 3600;
		}
		if (elapsedSeconds >= 60) {
			minutes = elapsedSeconds / 60;
			elapsedSeconds -= minutes * 60;
		}
		seconds = elapsedSeconds;

		if (hours > 0) {
			return formatElapsedTime(FAST_FORMAT_HMMSS, hours, minutes, seconds);
		} else {
			return formatElapsedTime(FAST_FORMAT_MMSS, minutes, seconds);
		}
	}

	private static String formatElapsedTime(String format, long hours,
			long minutes, long seconds) {
		if (FAST_FORMAT_HMMSS.equals(format)) {
			StringBuffer sb = null;
			if (sb == null) {
				sb = new StringBuffer(8);
			}
			sb.append(hours);
			sb.append(TIME_SEPARATOR);
			if (minutes < 10) {
				sb.append(TIME_PADDING);
			} else {
				sb.append(toDigitChar(minutes / 10));
			}
			sb.append(toDigitChar(minutes % 10));
			sb.append(TIME_SEPARATOR);
			if (seconds < 10) {
				sb.append(TIME_PADDING);
			} else {
				sb.append(toDigitChar(seconds / 10));
			}
			sb.append(toDigitChar(seconds % 10));
			return sb.toString();
		} else {
			return String.format(Locale.US, format, hours, minutes, seconds);
		}
	}

	private static char toDigitChar(long digit) {
		return (char) (digit + '0');
	}

	private static String formatElapsedTime(String format, long minutes,
			long seconds) {
		if (FAST_FORMAT_MMSS.equals(format)) {
			StringBuffer sb = null;
			if (sb == null) {
				sb = new StringBuffer(8);
			}
			if (minutes < 10) {
				sb.append(TIME_PADDING);
			} else {
				sb.append(toDigitChar(minutes / 10));
			}
			sb.append(toDigitChar(minutes % 10));
			sb.append(TIME_SEPARATOR);
			if (seconds < 10) {
				sb.append(TIME_PADDING);
			} else {
				sb.append(toDigitChar(seconds / 10));
			}
			sb.append(toDigitChar(seconds % 10));
			return sb.toString();
		} else {
			return String.format(format, minutes, seconds);
		}
	}

	public static String getTimeMusic(int sec) {

		int minute = sec / MINUTE;
		int second = sec - minute * MINUTE;
		String format;
		if (second >= 0 & second < 10) {
			format = ":0";
		} else {
			format = ":";
		}
		if (minute > Constants.ONE_SECOND) {
			return "";
		} else {
			return "(" + minute + format + second + ")";
		}

	}

	@SuppressWarnings("deprecation")
	public static void setBackground(View image, Drawable drawable) {
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			image.setBackgroundDrawable(drawable);
		} else {
			image.setBackground(drawable);
		}
	}

	public static String getElevation(double elevationInMeters, boolean isMetric) {
		if (isMetric) {
			return new DecimalFormat("0.#").format(elevationInMeters);
		}
		return String.valueOf(Math.round(elevationInMeters
				* UnitConversions.M_TO_FT));
	}

	public static double getElevationDouble(double elevationInMeters,
			boolean isMetric) {
		if (isMetric) {
			return elevationInMeters;
		}
		return elevationInMeters * UnitConversions.M_TO_FT;
	}

	// 1983-05-17
	public static String getDateFormatYYYYMMDD(Date date) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		return dateFormat.format(date);
	}

	// api/v1/...././
	// return .
	public static String getLastCompanion(String url) {
		Uri uri = Uri.parse(url);

		return uri.getPathSegments().get(uri.getPathSegments().size() - 1);

	}

	public static int getLastCompanionInt(String url) {
		Uri uri = Uri.parse(url);

		return Integer.valueOf(uri.getPathSegments().get(
				uri.getPathSegments().size() - 1));

	}

	public static String getDateFormatYYYYMMDDtHHMMSS(Date date) {
		if (date == null) {
			date = new Date();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		return dateFormat.format(date);

	}

	public static String getDateFormatMMMMyyyy(String time) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy",
				Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(new Date(getLongYYYYMMDDtHHMMSS(time)));
	}

	@SuppressWarnings("deprecation")
	public static String getDateHHMM(long dateLong, Context ctx) {
		SimpleDateFormat formatter;
		if (!DateFormat.is24HourFormat(ctx)) {
			formatter = new SimpleDateFormat("hh:mm a", Locale.US);
		} else {
			formatter = new SimpleDateFormat("HH:mm", Locale.US);
		}
		SimpleDateFormat formatterDay = new SimpleDateFormat("dd/MM/yy",
				Locale.US);
		Date date = new Date(dateLong);
		String dateString = formatter.format(date);
		if (date.getDay() != new Date().getDay()) {
			if (getDay(new Date().getTime()) - getDay(date.getTime()) == 1) {
				return ctx.getString(R.string.yesterday);
			} else {
				return formatterDay.format(date);
			}
		} else {
			return dateString;
		}
	}

	private static int getDay(long millisec) {

		return (int) (millisec / 86400000);
	}

	private static int getWeek(long millisec) {

		return (int) (getDay(millisec) / 7);
	}

	private static int getMonth(long millisec) {

		return (int) (getWeek(millisec) / 4);
	}

	private static int getHour(long millisec) {

		return (int) (millisec / (86400000 / 24));
	}

	private static int getMinute(long millisec) {

		return (int) (millisec / ((86400000 / 24 / 60)));
	}

	/**
	 * Convert dp->px
	 */
	public static int getPX(int dp, Context ctx) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				ctx.getResources().getDisplayMetrics());

	}

	public static int calculateCalories(double weightLbs,
			long durationInSecnod, float distanceInMeters) {

		float calPerHour = BASE_CAL_PER_HOUR;
		float hours = (float) durationInSecnod / 60f / 60f;
		float paceMph = 0;
		if (hours != 0) {
			paceMph = (float) ((distanceInMeters * UnitConversions.M_TO_MI) / hours);
		}
		calPerHour += ((float) ((weightLbs) - BASE_WEIGHT_LBS) / 10) * 60;
		calPerHour += (paceMph - BASE_PACE_MPH) * 100;

		int result = (int) (calPerHour * hours);
		if (result < 0) {
			return 0;
		} else {
			return result;
		}
	}

	/**
	 * get countdown by second
	 */
	public static String getCountdownText(int second, Context ctx) {
		switch (second) {
		case 0:
			return ctx.getString(R.string.btn_none);

		case 3:
			return ctx.getString(R.string.btn_3_seconds);
		case 5:
			return ctx.getString(R.string.btn_5_seconds);
		case 10:

			return ctx.getString(R.string.btn_10_seconds);
		case 30:

			return ctx.getString(R.string.btn_30_seconds);
		default:
			return ctx.getString(R.string.btn_none);

		}

	}

	public static String getDateMSS(long time, Context ctx) {
		Date date = new Date(time * oneSecLong);
		String dateString;
		SimpleDateFormat formatter;
		if (getHour(time * oneSecLong) > 0
				&& getHour(time * oneSecLong) < hours24) {
			formatter = new SimpleDateFormat("H:mm:ss", Locale.US);
		} else {
			formatter = new SimpleDateFormat("m:ss", Locale.US);
		}

		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateString = formatter.format(date);
		if (getHour(time * oneSecLong) > hours23) {
			return String.valueOf(getHour(time * oneSecLong)) + ":"
					+ dateString;
		} else {
			return dateString;
		}

	}

	public static String getMiOrKm(boolean isMetric) {

		if (isMetric) {
			return " km / ";
		} else {
			return " mi / ";
		}

	}

	public static String getDateMSSQuote(long time, Context ctx) {
		Date date = new Date(time * oneSecLong);
		String dateString;
		SimpleDateFormat formatter;
		if (getHour(time * oneSecLong) > 0) {
			formatter = new SimpleDateFormat("H.mm.ss", Locale.US);
		} else {
			formatter = new SimpleDateFormat("m.ss", Locale.US);
		}

		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateString = formatter.format(date);
		return dateString.replace(".", "'");

	}

	public static long getLongYYYYMMDDtHHMMSS(String time) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		Date date = null;
		try {
			date = dateFormat.parse(time);

		} catch (ParseException e) {

		}
		return date.getTime();
	}

	public static long getLongMMMMyyyy(String time) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy",
				Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = dateFormat.parse(time);

			if (date == null) {
				dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				date = dateFormat.parse(time);
			}

		} catch (ParseException e) {

		}
		return date.getTime();
	}

	public static String getMonthDayDotTime(String time, Context ctx) {
		SimpleDateFormat formatter;
		if (!DateFormat.is24HourFormat(ctx)) {
			formatter = new SimpleDateFormat("hh:mm a", Locale.US);
		} else {
			formatter = new SimpleDateFormat("HH:mm", Locale.US);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM dd",
				Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = dateFormat.parse(time);
		} catch (ParseException e) {

		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(monthFormat.format(date));
		buffer.append(Constants.DOT);
		buffer.append(formatter.format(date));
		return buffer.toString();
	}

	public static float convertMetersToMiles(double meters, boolean isMetric) {
		double k;
		if (isMetric) {
			k = metersToKm;
		} else {
			k = metersToMiles;
		}
		BigDecimal x = new BigDecimal(meters * k);
		x = x.setScale(1, BigDecimal.ROUND_HALF_UP);
		return x.floatValue();

	}

	public static int convertMetersToMilesAVG(double meters, boolean isMetric) {
		if (isMetric) {
			return (int) Math.round(meters * metersToKm);
		} else {
			return (int) Math.round(meters * metersToMiles);
		}

	}

	public static double getK(boolean isMetric) {
		if (isMetric)
			return metersToKm;
		else
			return metersToMiles;
	}

	public static String getLastSeen(long dateLong, Context ctx) {

		Date date = new Date(dateLong);

		Date currentDate = new Date();
		if (getHour(currentDate.getTime() - date.getTime()) > hours24) {
			if (getDay(currentDate.getTime() - date.getTime()) > WEEK) {
				if (getWeek(currentDate.getTime() - date.getTime()) > fourDay) {
					// month
					int month = getMonth(currentDate.getTime() - date.getTime());
					int week = getWeek(currentDate.getTime() - date.getTime())
							- month * fourDay;

					StringBuffer weeks = new StringBuffer();
					if (week != 0) {
						weeks.append(", ");
						weeks.append(week + " ");
						if (week > 1) {
							weeks.append(ctx.getString(R.string.lbl_weeks));
						} else {
							weeks.append(ctx.getString(R.string.lbl_week));
						}
						weeks.append(" ");
					} else {
						weeks.append(" ");
					}
					if (month > 1) {
						return month + " " + ctx.getString(R.string.lbl_months)
								+ weeks + ctx.getString(R.string.lbl_ago);
					} else {
						return month + " " + ctx.getString(R.string.lbl_month)
								+ weeks + ctx.getString(R.string.lbl_ago);
					}
				} else {
					// week
					int week = getWeek(currentDate.getTime() - date.getTime());
					int day = getDay(currentDate.getTime() - date.getTime())
							- week * WEEK;

					StringBuffer days = new StringBuffer();
					if (day != 0) {
						days.append(", ");
						days.append(day + " ");
						if (day > 1) {
							days.append(ctx.getString(R.string.lbl_days));
						} else {
							days.append(ctx.getString(R.string.lbl_day));
						}
						days.append(" ");
					} else {
						days.append(" ");
					}
					if (week > 1) {
						return week + " " + ctx.getString(R.string.lbl_weeks)
								+ days + ctx.getString(R.string.lbl_ago);
					} else {
						return week + " " + ctx.getString(R.string.lbl_week)
								+ days + ctx.getString(R.string.lbl_ago);
					}

				}
			} else {
				// days
				int day = getDay(currentDate.getTime() - date.getTime());

				if (day > 1) {
					return day + " " + ctx.getString(R.string.lbl_days) + " "
							+ ctx.getString(R.string.lbl_ago);
				} else {
					return day + " " + ctx.getString(R.string.lbl_day) + " "
							+ ctx.getString(R.string.lbl_ago);
				}
			}
		} else {// today
			if (getHour(currentDate.getTime() - date.getTime()) >= 1) {
				// hours
				if (getHour(currentDate.getTime() - date.getTime()) > 1) {
					return getHour(currentDate.getTime() - date.getTime())
							+ " " + ctx.getString(R.string.lbl_hours) + " "
							+ ctx.getString(R.string.lbl_ago);
				} else {
					return ctx.getString(R.string.lbl_about_an) + " "
							+ ctx.getString(R.string.lbl_hour) + " "
							+ ctx.getString(R.string.lbl_ago);
				}
			} else {
				// minutes
				if (getMinute(currentDate.getTime() - date.getTime()) > 1) {
					return getMinute(currentDate.getTime() - date.getTime())
							+ " " + ctx.getString(R.string.lbl_minutes) + " "
							+ ctx.getString(R.string.lbl_ago);
				} else {
					return "1 " + ctx.getString(R.string.lbl_minute) + " "
							+ ctx.getString(R.string.lbl_ago);
				}
			}
		}

	}

	/*
	 * set run type images
	 */
	public static void setType(TextView imType, WorkOut item, TextView txtBaloom, Resources resources) {
        String activityTypeName = item.getActivityType();
        String activitySubTypeName = item.getActivitySubType();
        ActivityType activityType;
        ActivityType.ActivitySubType activitySubType;
        int subtypeColorResource;
        try {
            activityType = ActivityType.getFromName(activityTypeName);
            try {
                activitySubType = activityType.getSubtype(activitySubTypeName);
                subtypeColorResource= activitySubType.getColorResource();
            } catch (InvalidActivitySubTypeException e) {
                subtypeColorResource = R.color.fallback_missing_activity_type;
                e.printStackTrace();
            }
        } catch (InvalidActivityTypeException e) {
            e.printStackTrace();
            activityType = ActivityType.getDefaultActivityType();
            subtypeColorResource = R.color.fallback_missing_activity_type;
        }
        String activityTypeChar = activityType.getCharRepresentation();
        imType.setText(activityTypeChar);

        // Set the color for the sub activity type
        imType.setTextColor(resources.getColor(subtypeColorResource));
        int backgroundRunType = R.drawable.arrowbaloon_1;
        setBackgroundAndText(
            txtBaloom, backgroundRunType,
            item.getActivitySubType()
        );
	}

	// set text and backgroud. ActivityTrackDetails
	private static void setBackgroundAndText(TextView txtBaloom, int drawable,
			String runType) {
		if (txtBaloom != null) {
			txtBaloom.setText(runType);
			txtBaloom.setBackgroundResource(drawable);
		}
	}

	// generating signature
	public static String computeHmac(String baseString, String key)
			throws NoSuchAlgorithmException, InvalidKeyException,
			IllegalStateException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance(algoritm);
		SecretKeySpec secret = new SecretKeySpec(key.getBytes(),
				mac.getAlgorithm());
		mac.init(secret);
		byte[] digest = mac.doFinal(baseString.getBytes());
		return URLEncoder.encode(Base64.encodeToString(digest, 0), "UTF8");
	}
}
