package com.athlete.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.athlete.db.DatabaseHelper;
import com.google.android.maps.MapActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

public class OrmLiteBaseMapActivity<H extends DatabaseHelper> extends
		MapActivity {
	public static String TAG = "OrmLiteBaseMapActivity";

	private volatile DatabaseHelper helper;
	private volatile boolean created = false;
	private volatile boolean destroyed = false;

	/**
	 * Get a helper for this action.
	 */
	public DatabaseHelper getHelper() {
		if (helper == null) {
			if (!created) {
				throw new IllegalStateException(
						"A call has not been made to onCreate() yet so the helper is null");
			} else if (destroyed) {
				throw new IllegalStateException(
						"A call to onDestroy has already been made and the helper cannot be used after that point");
			} else {
				throw new IllegalStateException(
						"Helper is null for some unknown reason");
			}
		} else {
			return helper;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (helper == null) {
			helper = getHelperInternal(this);
			created = true;
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "On destroy");
		super.onDestroy();
		if (helper != null && helper.isOpen()) {
			releaseHelper(helper);
		}
		destroyed = true;
	}

	/**
	 * This is called internally by the class to populate the helper object
	 * instance. This should not be called directly by client code unless you
	 * know what you are doing. Use {@link #getHelper()} to get a helper
	 * instance. If you are managing your own helper creation, override this
	 * method to supply this activity with a helper instance.
	 * 
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to
	 * override the {@link #releaseHelper(OrmLiteSqliteOpenHelper)} method as
	 * well.
	 * </p>
	 */

	protected DatabaseHelper getHelperInternal(Context context) {

		DatabaseHelper newHelper = (DatabaseHelper) OpenHelperManager
				.getHelper(context, DatabaseHelper.class);
		return newHelper;

	}

	/**
	 * Release the helper instance created in
	 * {@link #getHelperInternal(Context)}. You most likely will not need to
	 * call this directly since {@link #onDestroy()} does it for you.
	 * 
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to
	 * override the {@link #getHelperInternal(Context)} method as well.
	 * </p>
	 */
	protected void releaseHelper(DatabaseHelper helper) {
		OpenHelperManager.releaseHelper();
		helper = null;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
