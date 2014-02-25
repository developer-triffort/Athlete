package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.MESSAGE)
public class Message implements Serializable {
	private static final long serialVersionUID = 8994070776407255999L;
	/**
	 * @author edBaev
	 */

	
	public final static String MESSAGE = "message";
	public final static String CREATED_DAY = "created_date";
	public final static String LAST_MESSAGE = "last_message";
	public final static String ID = "id";
	public final static String CONVERSATION = "conversation";
	public final static String USER = "user";
	public final static String USER_DELETED_NAME = "user_deleted_name";
	
	@DatabaseField(columnName = ID, id = true)
	private int id;

	@DatabaseField(columnName = CREATED_DAY)
	private String createdDate;
	@DatabaseField(columnName = MESSAGE)
	private String message;
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(columnName = CONVERSATION, foreign = true, foreignAutoRefresh = true)
	private Conversation conversation;
	@DatabaseField(columnName = USER_DELETED_NAME)
	private String userDeletedName;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public String getUserDeletedName() {
		return userDeletedName;
	}

	public void setUserDeletedName(String userDeletedName) {
		this.userDeletedName = userDeletedName;
	}

}
