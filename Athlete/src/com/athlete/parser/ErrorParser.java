package com.athlete.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author edBaev
 */
public class ErrorParser {
	public static final String ERROR = "error";
	public static final String MESSAGE = "message";
	public static final String CREATED = "created";

	// {"error": {"code": "100", "message": "Email or password incorrect."}}

	public String errorParser(final String json) {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(json);
			JSONObject jsonError = jsonObj.getJSONObject(ERROR);
			if (jsonError.has(MESSAGE)) {
				return jsonError.getString(MESSAGE);
			}
		} catch (JSONException e) {

		}
		return null;

	}
}