package com.athlete.parser;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.athlete.Constants;
import com.athlete.bl.BaseBl;
import com.athlete.model.FaceBookUser;

public class FaceBookUserParser {
	/**
	 * @author edBaev
	 */
	private final static String UID = "uid";

	protected static final String NAME = "name";
	protected static final String PIC = "pic";

	public List<FaceBookUser> getFBUser(final String json, BaseBl baseBl,
			String currentID, boolean isLikeRunning, List<String> fbHide) {
		List<FaceBookUser> listUser = new LinkedList<FaceBookUser>();

		try {

			JSONArray jsonArr = new JSONArray(json);
			for (int i = 0; i < jsonArr.length(); i++) {

				JSONObject jsonObject = jsonArr.getJSONObject(i);
				if (!fbHide.contains(jsonObject.getString(UID))) {
					FaceBookUser user = new FaceBookUser();
					if (isLikeRunning) {
						user.setId(Constants.FB_TAG + jsonObject.getString(UID));
					} else {
						user.setId(jsonObject.getString(UID));
					}
					user.setFirstName(jsonObject.getString(NAME));
					user.setProfileImage225url(jsonObject.getString(PIC));
					user.setCurrentUserId(currentID);
					user.setLikeRunning(isLikeRunning);
					baseBl.createOrUpdate(FaceBookUser.class, user);
					listUser.add(user);
				}
			}

		} catch (Exception e) {
		}
		return listUser;
	}
}
