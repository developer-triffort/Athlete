package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName =Constants.TABLE_NAME.USER_M2M_FEED)
public class UserM2MFeed extends BaseTable implements Serializable {
	private static final long serialVersionUID = 8859520458582259077L;
	/**
	 * @author edBaev
	 * 
	 */
	public static final String USER = "user";
	public static final String FEED = "feed";
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(columnName = FEED, foreign = true, foreignAutoRefresh = true)
	private Feed feed;

	public UserM2MFeed(User user, Feed feed) {
		super();
		this.setFeed(feed);
		this.user = user;
		generateId();
	}

	public UserM2MFeed() {
		super();
	}

	public void generateId() {
		if (user != null && getFeed() != null && user.getId() != null && getFeed().getId() != 0)
			setId(user.getId() + getFeed().getId());
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

}
