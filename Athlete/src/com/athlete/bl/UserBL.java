package com.athlete.bl;

import java.sql.SQLException;
import java.util.List;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.User;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class UserBL extends BaseOperationsBL {
	private Dao<User, String> mDAO;


	public UserBL(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(User.class);
		} catch (SQLException e) {
		
		}
	}

	public boolean createOrUpdate(User obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public List<User> getListFromDBByFullname(Object value) {
		
		List<User> result = null;
		try {
		
			result = mDAO.queryBuilder().where().like(User.FIRST_NAME, "%"+value+"%")
					.or().like(User.LAST_NAME, "%"+value+"%").query();
		} catch (Exception e) {
		}
		return result;
	}

	public User getBy(String id) {
		User result = null;
		try {

			result = mDAO.queryForId(id);
		} catch (Exception e) {
			
		}
		return result;
	}

}
