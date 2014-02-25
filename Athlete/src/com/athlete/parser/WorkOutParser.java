package com.athlete.parser;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.athlete.bl.BaseBl;
import com.athlete.model.WorkOut;
import com.athlete.util.CommonHelper;

public class WorkOutParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";

	public WorkOut getWorkout(final JSONObject json) {
		WorkOut workOut = new WorkOut();

		try {
			if (json.has(WorkOut.DISTANCE) && !json.isNull(WorkOut.DISTANCE)) {
				workOut.setDistance(json.getDouble(WorkOut.DISTANCE));
			}

			if (json.has(WorkOut.ID)) {
				workOut.setId(json.getInt(WorkOut.ID));
			}
			if (json.has(WorkOut.USER)) {
				workOut.setIdUser(CommonHelper.getLastCompanion(json
						.getString(WorkOut.USER)));
			}

			if (json.has(WorkOut.POST_BODY) && !json.isNull(WorkOut.POST_BODY)) {
				workOut.setPostBody(json.getString(WorkOut.POST_BODY));
			}
			if (json.has(WorkOut.RUN_DATE)) {
				workOut.setRunDate(json.getString(WorkOut.RUN_DATE));
			}

            if (json.has(WorkOut.ACTIVITY_TYPE)) {
                workOut.setActivityType(json.getString(WorkOut.ACTIVITY_TYPE));
            }
            if (json.has(WorkOut.ACTIVITY_SUBTYPE)) {
                workOut.setActivitySubType(json.getString(WorkOut.ACTIVITY_SUBTYPE));
            }

			if (json.has(WorkOut.SOURCE)) {
				workOut.setSource(json.getString(WorkOut.SOURCE));
			}
			if (json.has(WorkOut.POST)) {
				workOut.setPost(CommonHelper.getLastCompanion(json
						.getString(WorkOut.POST)));
			}
			if (json.has(WorkOut.TITLE) && !json.isNull(WorkOut.TITLE)) {
				workOut.setTitle(json.getString(WorkOut.TITLE));
			}
			if (json.has(WorkOut.DURATION) && !json.isNull(WorkOut.DURATION)) {
				workOut.setDuration(json.getLong(WorkOut.DURATION));
			}
			if (json.has(WorkOut.CALORIES) && !json.isNull(WorkOut.CALORIES)) {
				workOut.setCalories(json.getLong(WorkOut.CALORIES));
			}

			if (json.has(WorkOut.ROUTE) && !json.isNull(WorkOut.ROUTE)) {
				JSONObject jsonRoute = json.getJSONObject(WorkOut.ROUTE);
				if (jsonRoute.has(WorkOut.IS_FAVORITE)
						&& !jsonRoute.isNull(WorkOut.IS_FAVORITE)) {
					workOut.setFavorite(jsonRoute
							.getBoolean(WorkOut.IS_FAVORITE));
				}
				if (jsonRoute.has(WorkOut.ID) && !jsonRoute.isNull(WorkOut.ID)) {
					workOut.setRouteID(jsonRoute.getInt(WorkOut.ID));
				}
				if (jsonRoute.has(WorkOut.STATIC_MAP_URL)
						&& !jsonRoute.isNull(WorkOut.STATIC_MAP_URL)) {
					workOut.setStaticMapUrl(jsonRoute
							.getString(WorkOut.STATIC_MAP_URL));
				}
			}
		} catch (Exception e) {
		}
		return workOut;
	}

	
	public List<WorkOut> getListWorkout(String response, BaseBl bl) {
		List<WorkOut> workOuts = new LinkedList<WorkOut>();

		try {
			JSONObject json = new JSONObject(response);
			JSONArray jsonObjects = json.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {
				WorkOut workOut = getWorkout(jsonObjects.getJSONObject(i));
				if (workOut != null) {
					bl.createOrUpdate(WorkOut.class, workOut);
					workOuts.add(workOut);
				}
			}

		} catch (Exception e) {
		}
		return workOuts;
	}
}
