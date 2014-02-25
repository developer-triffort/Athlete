package com.athlete.parser;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.athlete.bl.BaseBl;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.exception.AuthException;
import com.athlete.model.User;
import com.athlete.util.CommonHelper;

public class UserParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";

	protected static final String PREFERENCES = "preferences";
	protected static final String PROFILE = "profile";

	public List<User> getUserSearch(final String json, BaseBl baseBl,
			String currentID) {
		List<User> listUser = new LinkedList<User>();
		JSONObject jsonObj;
		boolean isCurrent = false;
		try {
			jsonObj = new JSONObject(json);
			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {

				User user = getUserDetails(jsonObjects.get(i).toString(),
						baseBl);
				baseBl.createOrUpdate(User.class, user);
				if (isCurrent)
					listUser.add(user);
				else {
					if (!user.getId().equals(currentID)) {
						listUser.add(user);
					} else {
						isCurrent = true;
					}
				}
			}

		} catch (Exception e) {
		}
		return listUser;
	}

	public User getUserDetails(String json, BaseOperationsBL baseBl)
			throws JSONException, AuthException {
		JSONObject jsonObj = new JSONObject(json);
		User user = new User();
		if (jsonObj.has(User.FIRST_NAME) && !jsonObj.isNull(User.FIRST_NAME)) {
			user.setFirstName(jsonObj.getString(User.FIRST_NAME));
		}
		if (jsonObj.has(User.EMAIL) && !jsonObj.isNull(User.EMAIL)) {
			user.setEmail(jsonObj.getString(User.EMAIL));
		}
		if (jsonObj.has(User.LAST_NAME) && !jsonObj.isNull(User.LAST_NAME)) {
			user.setLastName(jsonObj.getString(User.LAST_NAME));
		}
		if (jsonObj.has(User.ID)) {
			user.setId(jsonObj.getString(User.ID));
		}
		if (jsonObj.has(PREFERENCES) && !jsonObj.isNull(PREFERENCES)) {
			user.setPreferenceID(CommonHelper.getLastCompanion(jsonObj
					.getString(PREFERENCES)));
		}
		if (jsonObj.has(PROFILE) && !jsonObj.isNull(PROFILE)) {
			user.setProfileID(CommonHelper.getLastCompanion(jsonObj
					.getString(PROFILE)));
		}
		if (jsonObj.has(User.PROFILE_IMAGE_225)
				&& !jsonObj.isNull(User.PROFILE_IMAGE_225)) {
			user.setProfileImage225url(jsonObj
					.getString(User.PROFILE_IMAGE_225));
		}
		if (jsonObj.has(User.PROFILE_IMAGE_48)
				&& !jsonObj.isNull(User.PROFILE_IMAGE_48)) {
			user.setProfileImage48url(jsonObj.getString(User.PROFILE_IMAGE_48));
		}
		if (jsonObj.has(User.RESOURCE_URI)
				&& !jsonObj.isNull(User.RESOURCE_URI)) {
			user.setResourceUri(jsonObj.getString(User.RESOURCE_URI));
		}
		if (jsonObj.has(User.STATS) && !jsonObj.isNull(User.STATS)) {
			user.setStats(jsonObj.getString(User.STATS));
		}

		return user;
	}
}
