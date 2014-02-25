package com.athlete.parser;

import org.json.JSONObject;

import com.athlete.model.PreferenceUser;

public class PreferenceParser {
	/**
	 * @author edBaev
	 */

	public PreferenceUser getPreferenceParser(final String json,
			PreferenceUser preferenceUser) {

		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);
			if (jsonObj.has(PreferenceUser.ANNUALLY_EMAIL_REPORT)
					&& !jsonObj.isNull(PreferenceUser.ANNUALLY_EMAIL_REPORT)) {
				preferenceUser.setAnnuallyEmailReport(jsonObj
						.getBoolean(PreferenceUser.ANNUALLY_EMAIL_REPORT));
			}

			if (jsonObj.has(PreferenceUser.DISTANCE)
					&& !jsonObj.isNull(PreferenceUser.DISTANCE)) {
				preferenceUser.setDistance(jsonObj
						.getString(PreferenceUser.DISTANCE));
			}

			if (jsonObj.has(PreferenceUser.ID)
					&& !jsonObj.isNull(PreferenceUser.ID)) {
				preferenceUser.setId(jsonObj.getString(PreferenceUser.ID));
			}

			if (jsonObj.has(PreferenceUser.ELEVATION)
					&& !jsonObj.isNull(PreferenceUser.ELEVATION)) {
				preferenceUser.setElevation(jsonObj
						.getString(PreferenceUser.ELEVATION));
			}
			if (jsonObj.has(PreferenceUser.MONTHLY_EMAIL_REPORT)
					&& !jsonObj.isNull(PreferenceUser.MONTHLY_EMAIL_REPORT)) {
				preferenceUser.setMonthlyEmailReport(jsonObj
						.getBoolean(PreferenceUser.MONTHLY_EMAIL_REPORT));
			}

			if (jsonObj.has(PreferenceUser.TIMEZONE)
					&& !jsonObj.isNull(PreferenceUser.TIMEZONE)) {
				preferenceUser.setTimezone(jsonObj
						.getString(PreferenceUser.TIMEZONE));
			}

			if (jsonObj.has(PreferenceUser.WEEKLY_EMAIL_REPORT)
					&& !jsonObj.isNull(PreferenceUser.WEEKLY_EMAIL_REPORT)) {
				preferenceUser.setWeeklyEmailReport(jsonObj
						.getBoolean(PreferenceUser.WEEKLY_EMAIL_REPORT));
			}
		}

		catch (Exception e) {
		}
		return preferenceUser;
	}
}
