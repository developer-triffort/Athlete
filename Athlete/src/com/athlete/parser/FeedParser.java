package com.athlete.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.athlete.bl.BaseBl;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.model.Feed;
import com.athlete.model.Feed2Type2User;
import com.athlete.model.User;
import com.athlete.model.UserM2MFeed;
import com.athlete.model.WorkOut;

public class FeedParser {
	/**
	 * @author edBaev
	 */
	private final static String OBJECTS = "objects";
	private final static String META = "meta";
	private final static String FeedType = "feed_type";
	private final static String FRIENDS = "friends";
	private final static String FEATURED = "featured";
	private final static String LOCAL = "local";
	private final static String PROFILE = "profile";
	private int typeFeed;

	public int getTypeFeed() {
		return typeFeed;
	}

	public List<Feed> getFeed(final String json, int id_user, BaseBl baseBl,
			int type, boolean isDelete, int fallBack) {
		typeFeed = type;
		List<Feed> listFeed = new LinkedList<Feed>();
		JSONObject jsonObj;

		try {
			jsonObj = new JSONObject(json);
			JSONArray jsonObjects = jsonObj.getJSONArray(OBJECTS);

			if (fallBack == 1) {
				JSONObject jsonMeta = jsonObj.getJSONObject(META);
				// 1-friends
				// 2-local
				// 3-featured
				// 0-profile
				String feedType = jsonMeta.getString(FeedType);

				if (FRIENDS.equalsIgnoreCase(feedType)) {
					typeFeed = 1;
				}
				if (FEATURED.equalsIgnoreCase(feedType)) {
					typeFeed = 3;
				}
				if (LOCAL.equalsIgnoreCase(feedType)) {
					typeFeed = 2;
				}
				if (PROFILE.equalsIgnoreCase(feedType)) {
					typeFeed = 0;
				}
			}

			if (isDelete) {
				baseBl.deleteBy2Field(Feed2Type2User.class,
						Feed2Type2User.TYPE, typeFeed,
						Feed2Type2User.CURR_USER, id_user);
			}
			for (int i = 0; i < jsonObjects.length(); i++) {

				listFeed.add(getFeed(jsonObjects.getJSONObject(i), id_user,
						baseBl, typeFeed));
			}

		} catch (Exception e) {
		}
		return listFeed;
	}

	public Feed getFeed(JSONObject jsonFeed, int id_user,
			BaseOperationsBL baseBl, int type) {
		Feed feed = new Feed();
		try {

			JSONObject jsonAuthor = jsonFeed.getJSONObject(Feed.AUTHOR);
			UserParser parser = new UserParser();
			User user = parser.getUserDetails(jsonAuthor.toString(), baseBl);
			baseBl.createOrUpdate(User.class, user);
			feed.setUser(user);
			if (jsonFeed.has(Feed.BODY)) {
				feed.setBody(jsonFeed.getString(Feed.BODY));
			}
			if (jsonFeed.has(Feed.COMMENT_COUNT)) {
				feed.setCommentCount(jsonFeed.getInt(Feed.COMMENT_COUNT));
			}
			if (jsonFeed.has(Feed.CREATED_DATE)) {
				feed.setCreatedDate(jsonFeed.getString(Feed.CREATED_DATE));
			}
			if (jsonFeed.has(Feed.DISPLAY_DATE)) {
				feed.setDisplayDate(jsonFeed.getString(Feed.DISPLAY_DATE));
			}

			feed.setId(jsonFeed.getInt(Feed.ID));
			feed.setIdUser(id_user);
			if (jsonFeed.has(Feed.LIKERS)) {
				JSONArray likers = jsonFeed.getJSONArray(Feed.LIKERS);
				ArrayList<Integer> likersArray = new ArrayList<Integer>();
				for (int j = 0; j < likers.length(); j++) {
					likersArray.add(likers.getInt(j));
				}
				feed.setLikers(likersArray);
			}
			// WORKOUT
			if (!jsonFeed.isNull(Feed.WORKOUT)) {
				JSONObject jsonObjectWorkout = jsonFeed
						.getJSONObject(Feed.WORKOUT);
				WorkOut out;
				WorkOutParser outParser = new WorkOutParser();
				out = outParser.getWorkout(jsonObjectWorkout);
				out.setFeed(feed);
				feed.setWorkOut(out);
				baseBl.createOrUpdate(WorkOut.class, out);
			}
			baseBl.createOrUpdate(Feed2Type2User.class, new Feed2Type2User(
					feed, String.valueOf(id_user), type, user.getId()));
			baseBl.createOrUpdate(UserM2MFeed.class,
					new UserM2MFeed(user, feed));
			baseBl.createOrUpdate(Feed.class, feed);

		} catch (Exception e) {
		}
		return feed;
	}
}
