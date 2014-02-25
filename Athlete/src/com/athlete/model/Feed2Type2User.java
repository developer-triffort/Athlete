package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.FEED2TYPE2USER)
public class Feed2Type2User extends BaseTable implements Serializable {

	private static final long serialVersionUID = 7502113278355463984L;
	/**
	 * @author edBaev
	 * 
	 */

	public static final String FEED = "feed";
	public static final String CURR_USER = "currUser";
	public static final String USER = "user";
	public static final String TYPE = "type";
	@DatabaseField(columnName = FEED, foreign = true, foreignAutoRefresh = true)
	private Feed feed;
	@DatabaseField(columnName = CURR_USER)
	private String currentUserId;
	@DatabaseField(columnName = USER)
	private String userId;
	/**
	 * @param 1-friends,2-local,3-featured, 0-userProfile
	 * 
	 */
	@DatabaseField(columnName = TYPE)
	private int type;

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public Feed2Type2User(Feed feed, String currentUserId, int type,
			String userId) {
		super();
		this.setCurrentUserId(currentUserId);
		this.setUserId(userId);
		this.feed = feed;
		this.setType(type);
		generateId();

	}

	public Feed2Type2User() {
		super();
	}

	public void generateId() {
		if (feed != null && getCurrentUserId() != null && feed.getId() != 0
				&& getCurrentUserId() != null)
			setId(feed.getId() + "m" + getCurrentUserId() + "t" + getType());
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
