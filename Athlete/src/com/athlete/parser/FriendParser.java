package com.athlete.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.athlete.bl.BaseOperationsBL;
import com.athlete.model.FriendsM2M;
import com.athlete.model.User;

public class FriendParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";
	private final static String META = "meta";
	private int count;
	private ArrayList<String> status = new ArrayList<String>();
	private ArrayList<String> friendShipId = new ArrayList<String>();
	private ArrayList<Integer> requester = new ArrayList<Integer>();

	public ArrayList<User> getFriends(final String json, BaseOperationsBL bl,
			String idUserDetails) {
		ArrayList<User> listFriend = new ArrayList<User>();
		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);
			JSONObject jsonMeta = jsonObj.getJSONObject(META);
			count = jsonMeta.getInt(FriendsM2M.TOTAL_FRIEND);
			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {
				listFriend.add(getFriend(bl, idUserDetails,
						jsonObjects.getJSONObject(i)));
			}

		} catch (Exception e) {
		}

		return listFriend;
	}

	public User getFriend(BaseOperationsBL bl, String idUserDetails,
			JSONObject jsonObject) {
		User user = null;
		JSONObject jsonAuthor;
		try {
			jsonAuthor = jsonObject.getJSONObject(User.USER1);

			UserParser parser = new UserParser();
			user = parser.getUserDetails(jsonAuthor.toString(), bl);
			if (user.getId().equals(idUserDetails)) {
				jsonAuthor = jsonObject.getJSONObject(User.USER2);
				user = parser.getUserDetails(jsonAuthor.toString(), bl);
			}
			bl.createOrUpdate(User.class, user);
			status.add(jsonObject.getString(FriendsM2M.STATUS));
			friendShipId.add(jsonObject.getString(FriendsM2M.FRIEND_SHIP_ID));
			requester.add(jsonObject.getInt(FriendsM2M.REQUESTER));
		} catch (Exception e) {
		}
		return user;

	}

	public int getTotalFriend() {
		return count;
	}

	public ArrayList<String> getStatus() {
		return status;
	}

	public ArrayList<String> getFriendShipId() {
		return friendShipId;
	}

	public ArrayList<Integer> getRequester() {
		return requester;
	}
}
