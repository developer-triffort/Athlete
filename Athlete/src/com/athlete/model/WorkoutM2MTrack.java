package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.WORKOUT_M2M_TRACK)
public class WorkoutM2MTrack extends BaseTable implements Serializable {

	private static final long serialVersionUID = 6414931112424601822L;
	/**
	 * @author edBaev
	 * 
	 */
	public static final String WORKOUT = "workout";
	public static final String TRACK = "track";
	@DatabaseField(columnName = WORKOUT)
	private int workoutID;
	@DatabaseField(columnName = TRACK)
	private long trackID;

	public int getWorkOutID() {
		return workoutID;
	}

	public void setWorkOutID(int workoutID) {
		this.workoutID = workoutID;
	}

	public long getTrackID() {
		return trackID;
	}

	public void setTrackID(long trackID) {
		this.trackID = trackID;
	}

	public WorkoutM2MTrack(int workoutID, long trackID) {
		super();
		this.setTrackID(trackID);
		this.setWorkOutID(workoutID);
		generateId();
	}

	public WorkoutM2MTrack() {
		super();
	}

	public void generateId() {
		if (workoutID != 0 && trackID != 0)
			setId(workoutID + "m" + trackID);
	}

}
