package com.athlete.bl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.athlete.activity.BaseActivity;
import com.athlete.db.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class BaseBl extends BaseOperationsBL {
	/**
	 * @author edBaev
	 */

	protected static DatabaseHelper helper;
	protected static Context mContext;

	public class JSON_KEY {

	}

	public BaseBl(BaseActivity context) {
		super(context.getHelper());

	}

	public static DatabaseHelper getHelper() {
		if (helper == null) {
			helper = OpenHelperManager.getHelper(
					mContext.getApplicationContext(), DatabaseHelper.class);
		}
		return helper;
	}

	public static <T> List<T> getAll(Class<T> clazz) {
		List<T> result = null;
		try {
			result = getHelper().getDao(clazz).queryForAll();
		} catch (SQLException e) {
		}
		if (result == null) {
			result = new ArrayList<T>();
		}
		return result;
	}

}
