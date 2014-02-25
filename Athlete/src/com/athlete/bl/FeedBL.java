package com.athlete.bl;

import java.sql.SQLException;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.Feed;
import com.athlete.model.WorkOut;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class FeedBL extends BaseOperationsBL {
	private Dao<Feed, String> mDAO;

	public FeedBL(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(Feed.class);
		} catch (SQLException e) {

		}
	}

	public boolean createOrUpdate(Feed obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	public Feed getBy(String id) {
		Feed result = null;
		try {

			result = mDAO.queryForId(id);
		} catch (Exception e) {

		}
		return result;
	}

	public Feed getByWorkout(WorkOut workOut) {
		Feed result = null;
		try {

			result = mDAO.queryForEq(Feed.WORKOUT, workOut).get(0);

		} catch (Exception e) {

		}
		return result;
	}

}
