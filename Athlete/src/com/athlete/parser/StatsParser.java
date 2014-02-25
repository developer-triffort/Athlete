package com.athlete.parser;

import org.json.JSONObject;

import com.athlete.model.Stats;

public class StatsParser {
	/**
	 * @author edBaev
	 */

	public Stats getStats(final String json) {
		Stats stats = new Stats();
		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);
			if (jsonObj.has(Stats.AVG_WEEKLY)
					&& !jsonObj.isNull(Stats.AVG_WEEKLY)) {
				stats.setAvgWeekly(jsonObj.getInt(Stats.AVG_WEEKLY));
			}
			if (jsonObj.has(Stats.BEST_10K) && !jsonObj.isNull(Stats.BEST_10K)) {
				stats.setBest10k(jsonObj.getInt(Stats.BEST_10K));
			}
			if (jsonObj.has(Stats.BEST_5K) && !jsonObj.isNull(Stats.BEST_5K)) {
				stats.setBest5k(jsonObj.getInt(Stats.BEST_5K));
			}
			if (jsonObj.has(Stats.BEST_HALF)
					&& !jsonObj.isNull(Stats.BEST_HALF)) {
				stats.setBesthalf(jsonObj.getInt(Stats.BEST_HALF));
			}
			if (jsonObj.has(Stats.BEST_MARATHON)
					&& !jsonObj.isNull(Stats.BEST_MARATHON)) {
				stats.setBestMarathon(jsonObj.getInt(Stats.BEST_MARATHON));
			}
			if (jsonObj.has(Stats.BEST_MILE)
					&& !jsonObj.isNull(Stats.BEST_MILE)) {
				stats.setBestMile(jsonObj.getInt(Stats.BEST_MILE));
			}
			if (jsonObj.has(Stats.ID) && jsonObj.isNull(Stats.ID)) {
				stats.setId(jsonObj.getInt(Stats.ID));
			}
			if (jsonObj.has(Stats.LATEST_WORKOUT)
					&& !jsonObj.isNull(Stats.LATEST_WORKOUT)) {
				stats.setLatestWorkoutDate(jsonObj
						.getString(Stats.LATEST_WORKOUT));
			}
			if (jsonObj.has(Stats.MAX_DISTANCE)
					&& !jsonObj.isNull(Stats.MAX_DISTANCE)) {
				stats.setMaxDistanceInMeters(jsonObj
						.getDouble(Stats.MAX_DISTANCE));
			}
			if (jsonObj.has(Stats.MAX_DURATION)
					&& !jsonObj.isNull(Stats.MAX_DURATION)) {
				stats.setMaxDurationInSeconds(jsonObj
						.getInt(Stats.MAX_DURATION));
			}
			if (jsonObj.has(Stats.MAX_ELEVATION)
					&& !jsonObj.isNull(Stats.MAX_ELEVATION)) {
				stats.setMax_elevation_gain_in_meters(jsonObj
						.getDouble(Stats.MAX_ELEVATION));
			}
			if (jsonObj.has(Stats.TOTAL_DISTANCE)
					&& !jsonObj.isNull(Stats.TOTAL_DISTANCE)) {
				stats.setTotalDistanceInMeters(jsonObj
						.getDouble(Stats.TOTAL_DISTANCE));
			}
		}

		catch (Exception e) {
		}
		return stats;
	}
}
