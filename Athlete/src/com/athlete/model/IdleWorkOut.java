package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.IDLE_WORKOUT)
public class IdleWorkOut implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4938690742048464666L;

	/**
	 * @author edBaev
	 */
	public static final String PHOTO = "photo";
	
	public static final  String TRACK_PATH = "track_path";
	public static final String PRIVACY = "privacy";
	public static final  String FB_ACCES = "fb_acces";
	public static final String USER = "user";
	public static final String NOW_DOWNLOAD = "now_download";
	public static final String ONLY_PHOTO = "onlyPhoto";
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(columnName = TRACK_PATH)
	private String trackPath;
	@DatabaseField(columnName = USER)
	private String idUser;
	@DatabaseField(columnName = WorkOut.POST)
	private String post;
	@DatabaseField(columnName = WorkOut.DISTANCE)
	private double distance;
	@DatabaseField(columnName = WorkOut.DURATION)
	private long duration;
	@DatabaseField(columnName = NOW_DOWNLOAD)
	private boolean nowDownload;
	@DatabaseField(columnName = ONLY_PHOTO)
	private boolean onlyPhoto;
	@DatabaseField(columnName = WorkOut.CALORIES)
	private long calories;
	@DatabaseField(columnName = WorkOut.POST_BODY)
	private String postBody;
	@DatabaseField(columnName = PRIVACY)
	private String privacy;

    @DatabaseField(columnName = WorkOut.ACTIVITY_TYPE)
    private String activityType = "";
    @DatabaseField(columnName = WorkOut.ACTIVITY_SUBTYPE)
    private String activitySubType = "";

	@DatabaseField(columnName = FB_ACCES)
	private String fbAcces;
	@DatabaseField(columnName = WorkOut.TITLE)
	private String title;
	@DatabaseField(columnName = PHOTO, dataType = DataType.SERIALIZABLE)
	private String[] photoPath;
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getPostBody() {
		return postBody;
	}

	public void setPostBody(String postBody) {
		this.postBody = postBody;
	}

    public String getActivityType() {
        return activityType;
    }

    public String getActivitySubType() {
        return activitySubType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setActivitySubType(String activitySubType) {
        this.activitySubType = activitySubType;
    }

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdUser() {
		return idUser;
	}

	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}

	public long getCalories() {
		return calories;
	}

	public void setCalories(long calories) {
		this.calories = calories;
	}

	public String getTrackPath() {
		return trackPath;
	}

	public void setTrackPath(String trackPath) {
		this.trackPath = trackPath;
	}

	public String getPrivacy() {
		return privacy;
	}

	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}

	public String getFbAcces() {
		return fbAcces;
	}

	public void setFbAcces(String fbAcces) {
		this.fbAcces = fbAcces;
	}

	public boolean isNowDownload() {
		return nowDownload;
	}

	public void setNowDownload(boolean nowDownload) {
		this.nowDownload = nowDownload;
	}

	public String[] getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String[] photoPath) {
		this.photoPath = photoPath;
	}

	public boolean isOnlyPhoto() {
		return onlyPhoto;
	}

	public void setOnlyPhoto(boolean onlyPhoto) {
		this.onlyPhoto = onlyPhoto;
	}

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

}
