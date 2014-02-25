package com.athlete.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.athlete.bl.BaseOperationsBL;
import com.athlete.model.Conversation;
import com.athlete.model.ConversationM2MUser;
import com.athlete.model.Message;
import com.athlete.model.User;

public class ConversationParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";

	public List<Conversation> getConversation(final String json,
			BaseOperationsBL baseBl, String currUserID) {
		List<Conversation> listConversation = new LinkedList<Conversation>();
		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);
			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {

				JSONObject jsonConversation = jsonObjects.getJSONObject(i);
				Conversation conversation = getOneConversation(
						jsonConversation, baseBl, currUserID);

				listConversation.add(conversation);
			}

		} catch (Exception e) {
		}
		return listConversation;
	}

	public Conversation getOneConversation(JSONObject jsonConversation,
			BaseOperationsBL baseBl, String currUserID) {

		Conversation conversation = new Conversation();

		try {

			JSONArray jsonAuthor = jsonConversation
					.getJSONArray(Conversation.RECIPIENTS);

			conversation.setArchived(jsonConversation
					.getBoolean(Conversation.ARCHIVED));
			conversation.setHasUnreadMessages(jsonConversation
					.getBoolean(Conversation.HAS_UNREAD_MESSAGES));
			conversation.setHidden(jsonConversation
					.getBoolean(Conversation.HIDDEN));
			conversation.setId(jsonConversation.getInt(Conversation.ID));
			conversation.setCreatedDate(jsonConversation
					.getString(Conversation.CREATED_DAY));
			JSONObject lastMessage = jsonConversation
					.getJSONObject(Conversation.LAST_MESSAGE);
			String usersMessageId = null;
			if (lastMessage.has(Message.USER)
					&& !lastMessage.isNull(Message.USER)) {
				usersMessageId = lastMessage.getString(Message.USER);
			}

			if (jsonConversation.has(Conversation.PAST_USERS_INVOLVED)
					&& !jsonConversation
							.isNull(Conversation.PAST_USERS_INVOLVED)) {
				ArrayList<String> pastUsers = new ArrayList<String>();
				JSONArray jsonArrayUsersPast = jsonConversation
						.getJSONArray(Conversation.PAST_USERS_INVOLVED);
				for (int i = 0; i < jsonArrayUsersPast.length(); i++) {
					pastUsers.add(jsonArrayUsersPast.getString(i));
				}
				conversation.setPastUsersInvolved(pastUsers);
			}
			User userMessage = null;
			for (int j = 0; j < jsonAuthor.length(); j++) {

				UserParser parser = new UserParser();
				User user = parser.getUserDetails(jsonAuthor.getJSONObject(j)
						.toString(), baseBl);
				baseBl.createOrUpdate(User.class, user);
				if (usersMessageId != null
						&& user.getResourceUri().equals(usersMessageId)) {
					userMessage = user;
				}
				baseBl.createOrUpdate(ConversationM2MUser.class,
						new ConversationM2MUser(conversation, user));
			}
			Message message = new MessageParser().getMessage(lastMessage);
			message.setConversation(conversation);
			message.setUser(userMessage);
			message.setUserDeletedName(lastMessage
					.getString(Message.USER_DELETED_NAME));

			baseBl.createOrUpdate(Message.class, message);
			conversation.setLastMessage(message);
			conversation.setCurrUserID(currUserID);
			baseBl.createOrUpdate(Conversation.class, conversation);

		} catch (Exception e) {
		}
		return conversation;
	}
}
