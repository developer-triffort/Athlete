package com.athlete.parser;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.athlete.bl.BaseOperationsBL;
import com.athlete.exception.AuthException;
import com.athlete.model.Comment;
import com.athlete.model.PostPicture;
import com.athlete.util.CommonHelper;

public class PictureParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";

	public List<PostPicture> getPostPictures(final String json,
			BaseOperationsBL baseBl) {
		List<PostPicture> postPictures = new LinkedList<PostPicture>();
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(json);
			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);
			for (int i = 0; i < jsonObjects.length(); i++) {
				PostPicture postPicture = getPostPicture(jsonObjects.get(i)
						.toString(), baseBl);
				postPictures.add(postPicture);
				baseBl.createOrUpdate(PostPicture.class, postPicture);
			}

		} catch (Exception e) {
		}
		return postPictures;
	}

	public PostPicture getPostPicture(String json, BaseOperationsBL baseBl)
			throws JSONException, AuthException {
		JSONObject jsonObj = new JSONObject(json);
		PostPicture postPicture = new PostPicture();
		if (jsonObj.has(PostPicture.DETAIL)
				&& !jsonObj.isNull(PostPicture.DETAIL)) {
			postPicture.setDetail(jsonObj.getString(PostPicture.DETAIL));
		}
		if (jsonObj.has(PostPicture.FEED) && !jsonObj.isNull(PostPicture.FEED)) {
			postPicture.setFeed(jsonObj.getString(PostPicture.FEED));
		}
		if (jsonObj.has(PostPicture.ORIGINAL)
				&& !jsonObj.isNull(PostPicture.ORIGINAL)) {
			postPicture.setOriginal(jsonObj.getString(PostPicture.ORIGINAL));
		}
		if (jsonObj.has(PostPicture.THUMBNAIL)
				&& !jsonObj.isNull(PostPicture.THUMBNAIL)) {
			postPicture.setThumbnail(jsonObj.getString(PostPicture.THUMBNAIL));
		}
		postPicture.setId(jsonObj.getInt(Comment.ID));
		postPicture.setFeedId(CommonHelper.getLastCompanionInt(jsonObj
				.getString(Comment.POST)));
		return postPicture;
	}
}
