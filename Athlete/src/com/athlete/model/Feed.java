package com.athlete.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.athlete.Constants;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.FEED)
public class Feed implements Serializable {
	/**
	 * @author edBaev
	 */
	private static final long serialVersionUID = 1158765895808192779L;
	public static final String ID = "id";
	public static final String AUTHOR = "author";
	public static final String AUTHORS = "authors";
	public static final String BODY = "body";
	public static final String COMMENT_COUNT = "comment_count";
	public static final String CREATED_DATE = "created_date";
	public static final String DISPLAY_DATE = "display_date";
	public static final String LIKERS = "likers";
	public static final String WORKOUT = "workout";
	public static final String WORKOUT_OBJ = "workout_obj";
	public static final String ID_USER = "id_user";
	
	@DatabaseField(id = true, columnName = ID)
	private int id;
	@DatabaseField(columnName = ID_USER)
	private int idCurrentUser;
	@DatabaseField(columnName = AUTHOR, dataType = DataType.SERIALIZABLE)
	private User user;
	@ForeignCollectionField(columnName = AUTHORS)
	private ForeignCollection<UserM2MFeed> userM2Mfeed;
	@DatabaseField(columnName = BODY)
	private String body;
	@DatabaseField(columnName = COMMENT_COUNT)
	private int commentCount;
	@DatabaseField(columnName = CREATED_DATE)
	private String createdDate;
	@DatabaseField(columnName = DISPLAY_DATE)
	private String displayDate;
	@DatabaseField(columnName = LIKERS, dataType = DataType.SERIALIZABLE)
	private ArrayList<Integer> likers;
	@DatabaseField(columnName = WORKOUT_OBJ, dataType = DataType.SERIALIZABLE)
	private WorkOut workOut;
	@ForeignCollectionField(columnName = WORKOUT)
	private ForeignCollection<WorkOut> workOuts;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getDisplayDate() {
		return displayDate;
	}

	public void setDisplayDate(String displayDate) {
		this.displayDate = displayDate;
	}

	public ArrayList<Integer> getLikers() {
		return likers;
	}

	public void setLikers(ArrayList<Integer> likers) {
		this.likers = likers;
	}


	public int getIdUser() {
		return idCurrentUser;
	}

	public void setIdUser(int idUser) {
		this.idCurrentUser = idUser;
	}

	public ForeignCollection<UserM2MFeed> getUserM2Mfeed() {
		return userM2Mfeed;
	}

	public void setUserM2Mfeed(ForeignCollection<UserM2MFeed> userM2Mfeed) {
		this.userM2Mfeed = userM2Mfeed;
	}

	public WorkOut getWorkOut() {
		return workOut;
	}

	public void setWorkOut(WorkOut workOut) {
		this.workOut = workOut;
	}

	

}
