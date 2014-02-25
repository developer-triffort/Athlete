package com.athlete.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.athlete.Constants;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.CONVERSATION)
public class Conversation implements Serializable {
	private static final long serialVersionUID = -3634502312031314081L;
	/**
	 * @author edBaev
	 */

	public final static String ARCHIVED = "archived";
	public final static String CREATED_DAY = "created_date";
	public final static String HAS_UNREAD_MESSAGES = "has_unread_messages";
	public final static String HIDDEN = "hidden";
	public final static String LAST_MESSAGE = "last_message";
	public final static String ID = "id";
	public static final String MESSAGE = "message";
	public final static String CURR_USER_ID = "currUserId";
	public final static String RECIPIENTS = "recipients";
	public final static String CONM2MUSER = "conv_m2m_user";
	public final static String PAST_USERS_INVOLVED = "past_users_involved";

	@DatabaseField(columnName = ARCHIVED)
	private boolean archived;

	@DatabaseField(columnName = HAS_UNREAD_MESSAGES)
	private boolean hasUnreadMessages;
	@DatabaseField(columnName = HIDDEN)
	private boolean hidden;

	@DatabaseField(columnName = ID, id = true)
	private int id;
	@DatabaseField(columnName = PAST_USERS_INVOLVED, dataType = DataType.SERIALIZABLE)
	private ArrayList<String> pastUsersInvolved;
	@DatabaseField(columnName = CREATED_DAY)
	private String createdDate;
	@DatabaseField(columnName = CURR_USER_ID)
	private String currUserID;
	@DatabaseField(columnName = LAST_MESSAGE, dataType = DataType.SERIALIZABLE)
	private Message lastMessage;
	@ForeignCollectionField(columnName = CONM2MUSER)
	private ForeignCollection<ConversationM2MUser> conv_m2m_user;

	public boolean isArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public boolean isHasUnreadMessages() {
		return hasUnreadMessages;
	}

	public void setHasUnreadMessages(boolean hasUnreadMessages) {
		this.hasUnreadMessages = hasUnreadMessages;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

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

	public Message getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(Message message) {
		this.lastMessage = message;
	}

	public ArrayList<String> getPastUsersInvolved() {
		return pastUsersInvolved;
	}

	public void setPastUsersInvolved(ArrayList<String> pastUsersInvolved) {
		this.pastUsersInvolved = pastUsersInvolved;
	}

	public String getCurrUserID() {
		return currUserID;
	}

	public void setCurrUserID(String currUserID) {
		this.currUserID = currUserID;
	}

}
