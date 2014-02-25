package com.athlete.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.athlete.bl.BaseOperationsBL;
import com.athlete.exception.AuthException;
import com.athlete.model.Comment;
import com.athlete.model.Feed;
import com.athlete.model.User;
import com.athlete.util.CommonHelper;

public class CommentParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";

	public List<Comment> getComments(final String json, BaseOperationsBL baseBl) {
		List<Comment> listComment = new LinkedList<Comment>();
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(json);
			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {

				Comment comment = getComment(jsonObjects.get(i).toString(),
						baseBl);
				baseBl.createOrUpdate(Comment.class, comment);
				listComment.add(comment);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return listComment;
	}

	public Comment getComment(String json, BaseOperationsBL baseBl)
			throws JSONException, AuthException {
		JSONObject jsonObj = new JSONObject(json);
		Comment comment = new Comment();
		if (jsonObj.has(Comment.COMMENT) && !jsonObj.isNull(Comment.COMMENT)) {
			comment.setComment(jsonObj.getString(Comment.COMMENT));
		}
		comment.setId(jsonObj.getInt(Comment.ID));

		if (jsonObj.has(Feed.LIKERS)) {
			JSONArray likers = jsonObj.getJSONArray(Feed.LIKERS);
			ArrayList<Integer> likersArray = new ArrayList<Integer>();
			for (int j = 0; j < likers.length(); j++) {
				likersArray.add(likers.getInt(j));
			}
			comment.setLikers(likersArray);
		}
		comment.setFeedId(CommonHelper.getLastCompanionInt(jsonObj
				.getString(Comment.POST)));
		UserParser parser = new UserParser();
		User user = parser.getUserDetails(jsonObj.getJSONObject(Comment.USER)
				.toString(), baseBl);
		baseBl.createOrUpdate(User.class, user);
		comment.setUser(user);
		return comment;
	}
}
