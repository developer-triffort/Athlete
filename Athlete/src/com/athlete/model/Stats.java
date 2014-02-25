package com.athlete.model;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.STATS)
public class Stats {
	/**
	 * @author edBaev
	 */

	public final static String AVG_WEEKLY = "avg_weekly_distance_in_meters";// 6437,
	public final static String BEST_10K = "best_10k_duration_in_seconds";// null,
	public final static String BEST_5K = "best_5k_duration_in_seconds";// 11374,
	public final static String BEST_HALF = "best_half_marathon_duration_in_seconds";// null,
	public final static String BEST_MARATHON = "best_marathon_duration_in_seconds";// null,
	public final static String BEST_MILE = "best_mile_duration_in_seconds";// 3661,
	public final static String ID = "id";// : 307,
	public final static String LATEST_WORKOUT = "latest_workout_date";// "2012-07-31T00:00:00",
	public final static String MAX_DISTANCE = "max_distance_in_meters";// "6437.38",
	public final static String MAX_DURATION = "max_duration_in_seconds";// 14644,
	public final static String MAX_ELEVATION = "max_elevation_gain_in_meters";// "0.00",
	// "resource_uri" : "/api/v1/stats/307/",
	public final static String TOTAL_DISTANCE = "total_distance_in_meters";// 6437,
	public final static String USER = "user";// "/api/v1/user/307/"

	@DatabaseField(columnName = AVG_WEEKLY)
	private int avgWeekly;

	public int getAvgWeekly() {
		return avgWeekly;
	}

	public void setAvgWeekly(int avgWeekly) {
		this.avgWeekly = avgWeekly;
	}

	public int getBest10k() {
		return best10k;
	}

	public void setBest10k(int best10k) {
		this.best10k = best10k;
	}

	public int getBest5k() {
		return best5k;
	}

	public void setBest5k(int best5k) {
		this.best5k = best5k;
	}

	public int getBesthalf() {
		return besthalf;
	}

	public void setBesthalf(int besthalf) {
		this.besthalf = besthalf;
	}

	public int getBestMarathon() {
		return bestMarathon;
	}

	public void setBestMarathon(int bestMarathon) {
		this.bestMarathon = bestMarathon;
	}

	public int getBestMile() {
		return bestMile;
	}

	public void setBestMile(int bestMile) {
		this.bestMile = bestMile;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLatestWorkoutDate() {
		return latestWorkoutDate;
	}

	public void setLatestWorkoutDate(String latestWorkoutDate) {
		this.latestWorkoutDate = latestWorkoutDate;
	}

	public double getMaxDistanceInMeters() {
		return maxDistanceInMeters;
	}

	public void setMaxDistanceInMeters(double maxMistanceInMeters) {
		this.maxDistanceInMeters = maxMistanceInMeters;
	}

	public int getMaxDurationInSeconds() {
		return maxDurationInSeconds;
	}

	public void setMaxDurationInSeconds(int maxDurationInSeconds) {
		this.maxDurationInSeconds = maxDurationInSeconds;
	}

	public double getMaxElevationGainInMeters() {
		return maxElevationGainInMeters;
	}

	public void setMax_elevation_gain_in_meters(double maxElevationGainInMeters) {
		this.maxElevationGainInMeters = maxElevationGainInMeters;
	}

	public double getTotalDistanceInMeters() {
		return totalDistanceInMeters;
	}

	public void setTotalDistanceInMeters(double totalDistanceInMeters) {
		this.totalDistanceInMeters = totalDistanceInMeters;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@DatabaseField(columnName = BEST_10K)
	private int best10k;
	@DatabaseField(columnName = BEST_5K)
	private int best5k;

	@DatabaseField(columnName = BEST_HALF)
	private int besthalf;
	@DatabaseField(columnName = BEST_MARATHON)
	private int bestMarathon;
	@DatabaseField(columnName = BEST_MILE)
	private int bestMile;
	@DatabaseField(columnName = ID, id = true)
	private int id;

	@DatabaseField(columnName = LATEST_WORKOUT)
	private String latestWorkoutDate;
	@DatabaseField(columnName = MAX_DISTANCE)
	private double maxDistanceInMeters;
	@DatabaseField(columnName = MAX_DURATION)
	private int maxDurationInSeconds;
	@DatabaseField(columnName = MAX_ELEVATION)
	private double maxElevationGainInMeters;
	@DatabaseField(columnName = TOTAL_DISTANCE)
	private double totalDistanceInMeters;

	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;
}
