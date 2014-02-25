package com.athlete.parser;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.athlete.bl.BaseBl;
import com.athlete.model.Conversation;
import com.athlete.model.Message;
import com.athlete.model.User;

public class MessageParser {
	/**
	 * @author edBaev
	 */

	private final static String OBJECTS = "objects";

	public List<Message> getMessages(String json, BaseBl baseBl,
			Conversation conversation) {
		List<Message> listMessage = new LinkedList<Message>();
		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);

			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {
				User userMessage = null;
				Message message = new Message();
				JSONObject jsonMessage = jsonObjects.getJSONObject(i);
				message = getMessage(jsonMessage);
				message.setConversation(conversation);
				String usersMessageId;
				if (jsonMessage.has(Message.USER)
						&& !jsonMessage.isNull(Message.USER)) {
					usersMessageId = jsonMessage.getString(Message.USER);
					userMessage = baseBl.getListFromDBByField(User.class,
							User.RESOURCE_URI, usersMessageId).get(0);
				}

				message.setUser(userMessage);
				message.setUserDeletedName(jsonMessage
						.getString(Message.USER_DELETED_NAME));
				baseBl.createOrUpdate(Message.class, message);
				listMessage.add(message);
			}
		} catch (Exception e) {
		}
		return listMessage;

	}

	public Message getMessage(JSONObject jsonObj) {
		Message message = new Message();

		try {
			message.setId(jsonObj.getInt(Message.ID));
			if (jsonObj.has(Message.CREATED_DAY)
					&& !jsonObj.isNull(Message.CREATED_DAY)) {
				message.setCreatedDate(jsonObj.getString(Message.CREATED_DAY));
			}
			if (jsonObj.has(Message.MESSAGE)
					&& !jsonObj.isNull(Message.MESSAGE)) {
				message.setMessage(jsonObj.getString(Message.MESSAGE));
			}
		}

		catch (Exception e) {
		}
		return message;
	}

}
