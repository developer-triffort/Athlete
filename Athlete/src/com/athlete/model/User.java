package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.USER)
public class User implements Serializable {
	private static final long serialVersionUID = -6711838490183356751L;
	/**
	 * @author edBaev
	 */
	public static final String USERNAME = "username";
	public static final String API_KEY = "api_key";
	public static final String FIRST_NAME = "first_name";
	public static final String ID = "id";
	public static final String LAST_NAME = "last_name";
	public static final String PREFERENCE = "preferenceUser";
	public static final String PREFERENCE_ID = "preferenceUserID";
	public static final String PREFERENCES = "preferencesUser";
	public static final String PROFILE = "profileUser";
	public static final String PROFILE_ID = "profileUserId";
	public static final String PROFILES = "profileUsers";
	public static final String PROFILE_IMAGE_225 = "profile_image_225_url";
	public static final String PROFILE_IMAGE_48 = "profile_image_48_url";
	public static final String RESOURCE_URI = "resource_uri";
	public static final String STATS = "stats";
	public static final String EMAIL = "email";
	
	public static final String PHOTO = "photo";
	public static final String FRIEND = "friend";
	public static final String FEED = "feed";
	public static final String STATS2M = "stats2m";
	public static final String MESSAGE2M = "message";
	public static final String USER1 = "user1";
	public static final String USER2 = "user2";
	public static final String COMMENT = "Comment";
	@DatabaseField(id = true, columnName = ID)
	private String id;

	@DatabaseField(columnName = FIRST_NAME)
	private String firstName;
	@DatabaseField(columnName = EMAIL)
	private String email;
	@DatabaseField(columnName = LAST_NAME)
	private String lastName;

	@DatabaseField(columnName = PROFILE_IMAGE_225)
	private String profileImage225url;
	@DatabaseField(columnName = PROFILE_IMAGE_48)
	private String profileImage48url;
	@DatabaseField(columnName = RESOURCE_URI)
	private String resourceUri;
	@DatabaseField(columnName = STATS)
	private String stats;
	@DatabaseField(columnName = PREFERENCE_ID)
	private String preferenceID;
	@DatabaseField(columnName = PROFILE_ID)
	private String profileID;
	@ForeignCollectionField(columnName = FRIEND)
	private ForeignCollection<FriendsM2M> friendsM2Ms;

	@ForeignCollectionField(columnName = FEED)
	private ForeignCollection<UserM2MFeed> userM2Mfeed;

	@ForeignCollectionField(columnName = STATS2M)
	private ForeignCollection<Stats> stats2m;
	@ForeignCollectionField(columnName = PREFERENCES)
	private ForeignCollection<PreferenceUser> preferenceUsers;

	@ForeignCollectionField(columnName = PROFILES)
	private ForeignCollection<ProfileUser> profileUsers;
	@ForeignCollectionField(columnName = COMMENT)
	private ForeignCollection<Comment> comments;
	@ForeignCollectionField(columnName = MESSAGE2M)
	private ForeignCollection<Message> messages;
	@DatabaseField(columnName = PROFILE, foreign = true, foreignAutoRefresh = true)
	private ProfileUser profileUser;

	@DatabaseField(columnName = PREFERENCE, foreign = true, foreignAutoRefresh = true)
	private PreferenceUser preferenceUser;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getProfileImage225url() {
		return profileImage225url;
	}

	public void setProfileImage225url(String profileImage225url) {
		this.profileImage225url = profileImage225url;
	}

	public String getProfileImage48url() {
		return profileImage48url;
	}

	public void setProfileImage48url(String profileImage48url) {
		this.profileImage48url = profileImage48url;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	public String getStats() {
		return stats;
	}

	public void setStats(String stats) {
		this.stats = stats;
	}

	public ForeignCollection<FriendsM2M> getFriendsM2Ms() {
		return friendsM2Ms;
	}

	public void setFriendsM2Ms(ForeignCollection<FriendsM2M> friendsM2Ms) {
		this.friendsM2Ms = friendsM2Ms;
	}

	public ForeignCollection<UserM2MFeed> getUserM2Mfeed() {
		return userM2Mfeed;
	}

	public void setUserM2Mfeed(ForeignCollection<UserM2MFeed> userM2Mfeed) {
		this.userM2Mfeed = userM2Mfeed;
	}

	public ForeignCollection<Stats> getStats2m() {
		return stats2m;
	}

	public void setStats2m(ForeignCollection<Stats> stats2m) {
		this.stats2m = stats2m;
	}

	public ForeignCollection<PreferenceUser> getPreferenceUsers() {
		return preferenceUsers;
	}

	public void setPreferenceUsers(
			ForeignCollection<PreferenceUser> preferenceUsers) {
		this.preferenceUsers = preferenceUsers;
	}

	public ProfileUser getProfileUser() {
		return profileUser;
	}

	public void setProfileUser(ProfileUser profileUser) {
		this.profileUser = profileUser;
	}

	public PreferenceUser getPreferenceUser() {
		return preferenceUser;
	}

	public void setPreferenceUser(PreferenceUser preferenceUser) {
		this.preferenceUser = preferenceUser;
	}

	public String getPreferenceID() {
		return preferenceID;
	}

	public void setPreferenceID(String preferenceID) {
		this.preferenceID = preferenceID;
	}

	public String getProfileID() {
		return profileID;
	}

	public void setProfileID(String profileID) {
		this.profileID = profileID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
