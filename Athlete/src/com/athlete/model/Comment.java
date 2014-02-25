package com.athlete.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.athlete.Constants;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.COMMENT)
public class Comment implements Serializable {
	private static final long serialVersionUID = -6120898249881831983L;
	/**
	 * @author edBaev
	 */

	public static final String ID = "id";
	public static final String FEED_ID = "feedId";
	public static final String USER = "user";
	public static final String COMMENT = "comment";
	public static final String LIKERS = "likers";
	public static final String POST = "post";
	public static final String RESOURCE_URI = "resource_uri";
	public static final String SUBMIT_DATE = "submit_date";
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = FEED_ID)
	private int feedId;
	@DatabaseField(columnName = COMMENT)
	private String comment;
	@DatabaseField(columnName = LIKERS, dataType = DataType.SERIALIZABLE)
	private ArrayList<Integer> likers;
	@DatabaseField(columnName = SUBMIT_DATE)
	private String submitDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public ArrayList<Integer> getLikers() {
		return likers;
	}

	public void setLikers(ArrayList<Integer> likers) {
		this.likers = likers;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getFeedId() {
		return feedId;
	}

	public void setFeedId(int feedId) {
		this.feedId = feedId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(String submitDate) {
		this.submitDate = submitDate;
	}

}
