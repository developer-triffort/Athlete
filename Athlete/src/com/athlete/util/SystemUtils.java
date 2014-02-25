package com.athlete.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.athlete.Constants;

public class SystemUtils {

	/**
	 * Get the Athlete version from the manifest.
	 * 
	 */
	public static String getMyTracksVersion(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					"com.athlete", PackageManager.GET_META_DATA);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			Log.w(Constants.TAG, "Failed to get version info.", e);
			return "";
		}
	}

	public static WakeLock acquireWakeLock(Activity activity, WakeLock wakeLock) {

		try {
			PowerManager pm = (PowerManager) activity
					.getSystemService(Context.POWER_SERVICE);
			if (pm == null) {

				return wakeLock;
			}
			if (wakeLock == null) {
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						Constants.TAG);

				return wakeLock;
			}
			if (!wakeLock.isHeld()) {
				wakeLock.acquire();
			}
		} catch (RuntimeException e) {
			Log.e(Constants.TAG, "LocationUtils: Caught unexpected exception: "
					+ e.getMessage(), e);
		}
		return wakeLock;
	}

	private SystemUtils() {
	}
}