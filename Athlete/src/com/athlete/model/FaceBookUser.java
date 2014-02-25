package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.FB_USER)
public class FaceBookUser implements Serializable {
	public static final String currentUserIdField = "currId";
	private static final long serialVersionUID = 90675838226038199L;
	public static final String FIRST_NAME = "first_name";
	public static final String ID = "id";
	public static final String PROFILE_IMAGE_225 = "profile_image_225_url";
	public static final String LIKE_RUNNING = "like_running";
	/**
	 * @author edBaev
	 */
	@DatabaseField(columnName = currentUserIdField)
	private String currentUserId;
	@DatabaseField(id = true, columnName = ID)
	private String id;
	@DatabaseField(columnName = PROFILE_IMAGE_225)
	private String profileImage225url;
	@DatabaseField(columnName = FIRST_NAME)
	private String firstName;
	@DatabaseField(columnName = LIKE_RUNNING)
	private boolean isLikeRunning;

	public String getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getProfileImage225url() {
		return profileImage225url;
	}

	public void setProfileImage225url(String profileImage225url) {
		this.profileImage225url = profileImage225url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isLikeRunning() {
		return isLikeRunning;
	}

	public void setLikeRunning(boolean isLikeRunning) {
		this.isLikeRunning = isLikeRunning;
	}
}
