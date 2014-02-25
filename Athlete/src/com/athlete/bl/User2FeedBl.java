package com.athlete.bl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.Feed;
import com.athlete.model.UserM2MFeed;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class User2FeedBl extends BaseOperationsBL {
	private Dao<UserM2MFeed, String> mDAO;

	public User2FeedBl(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(UserM2MFeed.class);
		} catch (SQLException e) {

		}
	}

	public boolean createOrUpdate(UserM2MFeed obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public ArrayList<Feed> getFeedByUser(String id) {
		List<UserM2MFeed> result = null;
		ArrayList<Feed> resultUser = new ArrayList<Feed>();
		try {

			result = mDAO.queryForEq(UserM2MFeed.USER, id);
		} catch (Exception e) {

		}

		for (int i = 0; i < result.size(); i++) {
			resultUser.add(result.get(i).getFeed());
		}
		return resultUser;
	}

}
