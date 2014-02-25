package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.FRIEND_M2M)
public class FriendsM2M extends BaseTable implements Serializable {
	private static final long serialVersionUID = 9091606034885700149L;
	/**
	 * @author edBaev
	 * 
	 */
	public static final String USER = "user";
	public static final String FRIEND = "friend";
	public static final String TOTAL_FRIEND = "total_count";
	public static final String REQUESTER = "requester";
	public static final String STATUS = "status";
	public static final String ACCEPTED = "Accepted";
	public static final String PENDING = "Pending";
	public static final String FRIEND_SHIP_ID = "id";
	public static final String FRIEND_SHIP_ID_COLUMN_NAME = "friendId";
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(columnName = FRIEND, foreign = true, foreignAutoRefresh = true)
	private User friend;
	@DatabaseField(columnName = TOTAL_FRIEND)
	private int totalFriend;
	@DatabaseField(columnName = REQUESTER)
	private int requester;
	@DatabaseField(columnName = STATUS)
	private String status;
	@DatabaseField(columnName = FRIEND_SHIP_ID_COLUMN_NAME)
	private String friendShipId;

	public FriendsM2M(User user, User friend, int totalFriend, int reqst,
			String status, String friendShipId) {
		super();
		this.setFriend(friend);
		this.user = user;
		this.setTotalFriend(totalFriend);
		this.setRequester(reqst);
		this.setStatus(status);
		this.setFriendShipId(friendShipId);
		generateId();
	}

	public FriendsM2M() {
		super();
	}

	public void generateId() {
		if (user != null && getFriend() != null && user.getId() != null
				&& getFriend().getId() != null)
			setId(user.getId() + "f" + getFriend().getId());
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getFriend() {
		return friend;
	}

	public void setFriend(User friend) {
		this.friend = friend;
	}

	public int getTotal_friend() {
		return totalFriend;
	}

	public void setTotalFriend(int total_friend) {
		this.totalFriend = total_friend;
	}

	public int getRequester() {
		return requester;
	}

	public void setRequester(int requester) {
		this.requester = requester;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFriendShipId() {
		return friendShipId;
	}

	public void setFriendShipId(String friendShipId) {
		this.friendShipId = friendShipId;
	}
}
