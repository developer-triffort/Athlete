package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.PREFERENCE_USER)
public class PreferenceUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3931149314622999578L;

	/**
	 * @author edBaev
	 */

	public static final String ID = "id";

	public static final String ANNUALLY_EMAIL_REPORT = "annually_email_report";
	public static final String DISTANCE = "distance";
	public static final String ELEVATION = "elevation";
	public static final String MONTHLY_EMAIL_REPORT = "monthly_email_report";
	public static final String TIMEZONE = "timezone";
	public static final String USER = "user";
	public static final String WEEKLY_EMAIL_REPORT = "weekly_email_report";

	@DatabaseField(id = true, columnName = ID)
	private String id;
	/* "distance": "miles" */
	@DatabaseField(columnName = DISTANCE)
	private String distance;
	/* "elevation": "feet", */
	@DatabaseField(columnName = ELEVATION)
	private String elevation;
	/* "timezone": "UTC" */
	@DatabaseField(columnName = TIMEZONE)
	private String timezone;
	@DatabaseField(columnName = ANNUALLY_EMAIL_REPORT)
	private boolean annuallyEmailReport;
	@DatabaseField(columnName = MONTHLY_EMAIL_REPORT)
	private boolean monthlyEmailReport;
	@DatabaseField(columnName = WEEKLY_EMAIL_REPORT)
	private boolean weeklyEmailReport;
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getElevation() {
		return elevation;
	}

	public void setElevation(String elevation) {
		this.elevation = elevation;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public boolean isWeeklyEmailReport() {
		return weeklyEmailReport;
	}

	public void setWeeklyEmailReport(boolean weeklyEmailReport) {
		this.weeklyEmailReport = weeklyEmailReport;
	}

	public boolean isMonthlyEmailReport() {
		return monthlyEmailReport;
	}

	public void setMonthlyEmailReport(boolean monthlyEmailReport) {
		this.monthlyEmailReport = monthlyEmailReport;
	}

	public boolean isAnnuallyEmailReport() {
		return annuallyEmailReport;
	}

	public void setAnnuallyEmailReport(boolean annuallyEmailReport) {
		this.annuallyEmailReport = annuallyEmailReport;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
