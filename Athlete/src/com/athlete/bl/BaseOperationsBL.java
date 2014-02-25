package com.athlete.bl;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.athlete.db.DatabaseHelper;
import com.athlete.model.User;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.DeleteBuilder;

public class BaseOperationsBL {
	/**
	 * @author edBaev
	 */
	private static final String TAG = BaseOperationsBL.class.getName();
	private static final String USER = "user";
	protected DatabaseHelper helper;

	public BaseOperationsBL(DatabaseHelper helper) {
		this.helper = helper;
	}

	public BaseOperationsBL() {
	}

	public <T> T getBy(Class<T> clazz, UUID id) {
		T result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			result = dao.queryForId(id);
		} catch (Exception e) {
		
		}
		return result;
	}

	public <T> T getByUserID(Class<T> clazz, User id) {
		T result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			result = dao.queryForEq(USER, id).get(0);
		} catch (Exception e) {
			

		}
		return result;
	}

	public <T> List<T> getListFromDBByField(Class<T> clazz, String field,
			Object value) {
		List<T> result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			result = dao.queryForEq(field, value);
		} catch (Exception e) {
		
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public <T> List<T> getListFromDBByFieldWithLimit(Class<T> clazz,
			String field, Object value) {
		List<T> result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			result = dao.queryBuilder().limit(20).where().eq(field, value)
					.query();
		} catch (Exception e) {
			
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public <T> List<T> getListFromDBBy2Field(Class<T> clazz, String field1,
			Object value1, String field2, Object value2) {
		List<T> result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			result = dao.queryBuilder().limit(20).where().eq(field1, value1)
					.and().eq(field2, value2).query();
		} catch (Exception e) {
			
		}
		return result;
	}

	public <T> T getFromDBByField(Class<T> clazz, String field, Object value) {
		T result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);

			if (dao.queryForEq(field, value).get(0) != null)
				result = dao.queryForEq(field, value).get(0);
		} catch (Exception e) {
			
		}
		return result;
	}

	public <T> List<T> getListFromDB(Class<T> clazz) {
		List<T> result = null;
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			result = dao.queryForAll();
		} catch (Exception e) {
			
		}
		return result;
	}

	public <T> boolean createOrUpdate(Class<T> clazz, T obj) {
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			dao.createOrUpdate(obj);
			return true;
		} catch (Exception e) {
			
		}
		return false;
	}

	public <T> boolean update(Class<T> clazz, T obj) {
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			dao.update(obj);
			return true;
		} catch (Exception e) {
		
		}
		return false;
	}

	public <T> void createList(final List<T> list, Class<T> clazz)
			throws SQLException {
		if (list == null) {
			return;
		}
		final Dao<T, UUID> dao = helper.getDao(clazz);
		try {
			dao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for (T item : list) {
						dao.createOrUpdate(item);
					}
					return null;
				}
			});
		} catch (Exception e) {
		
		}

	}

	public <T> void updateList(final List<T> list, Class<T> clazz)
			throws SQLException {
		if (list == null) {
			return;
		}
		final Dao<T, UUID> dao = helper.getDao(clazz);
		try {
			dao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for (T item : list) {
						dao.update(item);
					}
					return null;
				}
			});
		} catch (Exception e) {
		}
	}

	public <T> void delete(T obj, Class<T> clazz) {
		
		if (obj == null) {
			return;
		}
		Dao<T, UUID> dao = null;
		try {
			dao = helper.getDao(clazz);
			dao.delete(obj);
		} catch (SQLException e) {
		

		}

	}

	public <T> void deleteByField(String field, Class<T> clazz, Object value) {
		if (field == null) {
			return;
		}
		Dao<T, UUID> dao = null;
		try {
			dao = helper.getDao(clazz);
			DeleteBuilder<T, UUID> deleteBuilder = dao.deleteBuilder();
			deleteBuilder.where().eq(field, value);
			dao.delete(deleteBuilder.prepare());

		} catch (SQLException e) {
			

		}

	}

	public <T> void deleteBy2Field(Class<T> clazz, String field1,
			Object value1, String field2, Object value2) {

		Dao<T, UUID> dao = null;
		try {
			dao = helper.getDao(clazz);
			DeleteBuilder<T, UUID> deleteBuilder = dao.deleteBuilder();
			deleteBuilder.where().eq(field1, value1).and().eq(field2, value2);
			dao.delete(deleteBuilder.prepare());

		} catch (SQLException e) {

		}

	}

	public <T> void delete(UUID arg0, Class<T> clazz) {
		if (arg0 == null) {
			return;
		}
		Dao<T, UUID> dao = null;
		try {
			dao = helper.getDao(clazz);
			dao.deleteById(arg0);
		} catch (SQLException e) {

		}

	}

	public <T> void deleteList(List<T> list, Class<T> clazz)
			throws SQLException {
		if (list == null || list.size() == 0) {
			return;
		}
		Dao<T, UUID> dao = helper.getDao(clazz);
		dao.delete(list);
	}

	public <T> void deleteCollection(ForeignCollection<T> col, Class<T> clazz)
			throws SQLException {
		if (col == null || col.size() == 0) {
			return;
		}
		Dao<T, UUID> dao = helper.getDao(clazz);
		dao.delete(col);
	}

	public <T> void refresh(Class<T> clazz, T object) {
		try {
			Dao<T, UUID> dao = helper.getDao(clazz);
			dao.refresh(object);
		} catch (Exception e) {
		}

	}

}
