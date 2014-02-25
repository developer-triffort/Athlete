package com.athlete.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.athlete.db.DatabaseHelper;
import com.athlete.exception.AuthException;
import com.athlete.exception.BaseException;
import com.athlete.model.User;

/**
 * @author edBaev
 */

public class BaseParser {
	protected JSONObject jsonObj;
	protected DatabaseHelper helper;

	public BaseParser(JSONObject jsonObj, DatabaseHelper helper) {
		this.jsonObj = jsonObj;
		this.helper = helper;
	}

	public BaseParser(final String json, DatabaseHelper helper)
			throws AuthException, BaseException {

		this.helper = helper;
		if (json != null) {
			JSONObject object = null;
			try {
				object = new JSONObject(json);
				jsonObj = object;
			} catch (Exception e) {

			}
		} else {
			jsonObj = new JSONObject();
		}

	}

	public String[] getUser() throws JSONException, AuthException {
		String[] user = new String[2];

		user[1] = (jsonObj.getString(User.API_KEY));
		user[0] = (jsonObj.getString(User.USERNAME));

		return user;
	}

	

}
