package com.athlete.parser;

import org.json.JSONObject;

import com.athlete.model.ProfileUser;

public class ProfileParser {
	/**
	 * @author edBaev
	 */

	public void getProfileUser(final String json, ProfileUser profileUser) {

		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);
			if (jsonObj.has(ProfileUser.BIRTH_DATE)
					&& !jsonObj.isNull(ProfileUser.BIRTH_DATE)) {
				profileUser.setBirthDate(jsonObj
						.getString(ProfileUser.BIRTH_DATE));
			}
			if (jsonObj.has(ProfileUser.GENDER)
					&& !jsonObj.isNull(ProfileUser.GENDER)) {
				profileUser.setGender(jsonObj.getString(ProfileUser.GENDER));
			}
			if (jsonObj.has(ProfileUser.ID) && !jsonObj.isNull(ProfileUser.ID)) {
				profileUser.setId(jsonObj.getString(ProfileUser.ID));
			}

			if (jsonObj.has(ProfileUser.WEIGHT)
					&& !jsonObj.isNull(ProfileUser.WEIGHT)) {
				profileUser.setWeight(jsonObj.getDouble(ProfileUser.WEIGHT));
			}
			if (jsonObj.has(ProfileUser.WEIGHT_UNIT)
					&& !jsonObj.isNull(ProfileUser.WEIGHT_UNIT)) {
				profileUser.setWeightUnit(jsonObj
						.getString(ProfileUser.WEIGHT_UNIT));
			}
			if (jsonObj.has(ProfileUser.LOCATION)
					&& !jsonObj.isNull(ProfileUser.LOCATION)) {
				JSONObject jsonLocation = new JSONObject(
						jsonObj.getString(ProfileUser.LOCATION));
				if (jsonLocation != null) {
					if (jsonLocation.has(ProfileUser.NAME)
							&& !jsonLocation.isNull(ProfileUser.NAME)) {
						profileUser.setLocationName(jsonLocation
								.getString(ProfileUser.NAME));
					}

					if (jsonLocation.has(ProfileUser.LAT)
							&& !jsonLocation.isNull(ProfileUser.LAT)) {
						profileUser.setLat(jsonLocation
								.getDouble(ProfileUser.LAT));
					}

					if (jsonLocation.has(ProfileUser.LNG)
							&& !jsonLocation.isNull(ProfileUser.LNG)) {
						profileUser.setLng(jsonLocation
								.getDouble(ProfileUser.LNG));
					}

				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
