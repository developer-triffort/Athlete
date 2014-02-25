package com.athlete.bl;

import java.sql.SQLException;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.WorkOut;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class WorkoutBL extends BaseOperationsBL {
	private Dao<WorkOut, String> mDAO;

	public WorkoutBL(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(WorkOut.class);
		} catch (SQLException e) {

		}
	}

	public boolean createOrUpdate(WorkOut obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public WorkOut getBy(String id) {
		WorkOut result = null;
		try {

			result = mDAO.queryForId(id);
		} catch (Exception e) {

		}
		return result;
	}

}
