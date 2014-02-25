package com.athlete.bl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.FriendsM2M;
import com.athlete.model.User;
import com.j256.ormlite.dao.Dao;

/**
 * @author edBaev
 */
public class FriendsBL extends BaseOperationsBL {
	private Dao<FriendsM2M, String> mDAO;

	private int count;

	public FriendsBL(DatabaseHelper helper) {
		super(helper);
		try {
			mDAO = helper.getDao(FriendsM2M.class);
		} catch (SQLException e) {

		}
	}

	public boolean createOrUpdate(FriendsM2M obj) {
		try {
			mDAO.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
			
		}
		return false;
	}

	public ArrayList<User> getFriendByUser(String id) {
		List<FriendsM2M> result = null;
		ArrayList<User> resultUser = new ArrayList<User>();
		try {

			result = mDAO.queryBuilder().where().eq(FriendsM2M.USER, id).and()
					.eq(FriendsM2M.STATUS, FriendsM2M.ACCEPTED).query();

		} catch (Exception e) {
			

		}
		if (result != null && !result.isEmpty()) {
			count = result.get(0).getTotal_friend();
			for (int i = 0; i < result.size(); i++) {
				resultUser.add(result.get(i).getFriend());
			}
		}
		return resultUser;
	}

	public ArrayList<User> getFriendByUserAccepted(String id) {
		List<FriendsM2M> result = null;
		ArrayList<User> resultUser = new ArrayList<User>();
		try {
			result = mDAO.queryBuilder().where().eq(FriendsM2M.USER, id).and()
					.eq(FriendsM2M.STATUS, FriendsM2M.ACCEPTED).query();

		} catch (Exception e) {
			

		}
		if (result != null && !result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				resultUser.add(result.get(i).getFriend());
			}
		}
		return resultUser;
	}

	public ArrayList<User> getFriendByUserPending(String id) {
		List<FriendsM2M> result = null;
		ArrayList<User> resultUser = new ArrayList<User>();
		try {

			result = mDAO.queryBuilder().where().eq(FriendsM2M.USER, id).and()
					.eq(FriendsM2M.STATUS, FriendsM2M.PENDING).and()
					.ne(FriendsM2M.REQUESTER, id).query();

		} catch (Exception e) {
			

		}
		if (result != null && !result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				resultUser.add(result.get(i).getFriend());
			}
		}
		return resultUser;
	}

	public List<FriendsM2M> getFriendM2M(String currUserId, String friendId) {
		List<FriendsM2M> result = null;
		try {

			result = mDAO.queryBuilder().where()
					.eq(FriendsM2M.USER, currUserId).and()
					.eq(FriendsM2M.FRIEND, friendId).query();
		} catch (Exception e) {
			

		}

		return result;
	}

	public int getCount() {
		return count;
	}

}
