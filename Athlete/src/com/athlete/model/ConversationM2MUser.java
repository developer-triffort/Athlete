package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.CONVERSATION_M2M_USER)
public class ConversationM2MUser extends BaseTable implements Serializable {

	private static final long serialVersionUID = 7502113278355463984L;
	/**
	 * @author edBaev
	 * 
	 */
	public static final String CONVERSATION = "conversation";
	public static final String USER = "user";
	@DatabaseField(columnName = CONVERSATION, foreign = true, foreignAutoRefresh = true)
	private Conversation conversation;
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ConversationM2MUser(Conversation conversation, User user) {
		super();
		this.setUser(user);
		this.conversation = conversation;
		generateId();
	}

	public ConversationM2MUser() {
		super();
	}

	public void generateId() {
		if (conversation != null && getUser() != null
				&& conversation.getId() != 0 && getUser().getId() != null)
			setId(conversation.getId() + "m" + getUser().getId());
	}

}
