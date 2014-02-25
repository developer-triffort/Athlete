package com.athlete.bl;

import java.sql.SQLException;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.Stats;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class StatsBL extends BaseOperationsBL {
	private Dao<Stats, String> mDAO;

	public StatsBL(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(Stats.class);
		} catch (SQLException e) {
		}
	}

	public boolean createOrUpdate(Stats obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public Stats getStatsByUser(String id) {
		Stats result = null;

		try {

			result = mDAO.queryForEq(Stats.USER, id).get(0);
		} catch (Exception e) {
			
		}

		return result;
	}

}
