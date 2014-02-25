package com.athlete.bl;

import java.sql.SQLException;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.IdleWorkOut;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class IdleWorkoutBL extends BaseOperationsBL {
	private Dao<IdleWorkOut, String> mDAO;
	

	public IdleWorkoutBL(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(IdleWorkOut.class);
		} catch (SQLException e) {
		
		}
	}

	public boolean createOrUpdate(IdleWorkOut obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
		
		}
		return false;
	}

	public IdleWorkOut getBy(String id) {
		IdleWorkOut result = null;
		try {

			result = mDAO.queryForId(id);
		} catch (Exception e) {
			
		}
		return result;
	}

}
