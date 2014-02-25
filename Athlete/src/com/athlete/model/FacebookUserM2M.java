package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.FB_USER_M2M)
public class FacebookUserM2M extends BaseTable implements Serializable {
	private static final long serialVersionUID = 8859520458582259077L;
	/**
	 * @author edBaev
	 * 
	 */
	public static final String FB_USER = "facebook_user";
	public static final String CURR_USER_ID = "curruserId";
	public static final String HIDE = "hide";
	@DatabaseField(columnName = FB_USER)
	private String user;
	@DatabaseField(columnName = CURR_USER_ID)
	private String curruserId;
	@DatabaseField(columnName = HIDE)
	private boolean hide;

	public FacebookUserM2M(String user, String currUserId, boolean hide) {
		super();
		this.setFacebookUserId(user);
		this.setHide(hide);
		this.setCurruserId(currUserId);
		generateId();
	}

	public FacebookUserM2M() {
		super();
	}

	public void generateId() {
		if (getUser() != null) {
			setId(getUser());
		}
	}

	public String getUser() {
		return user;
	}

	public void setFacebookUserId(String user) {
		this.user = user;
	}

	public boolean isHide() {
		return hide;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
	}

	public String getCurruserId() {
		return curruserId;
	}

	public void setCurruserId(String curruserId) {
		this.curruserId = curruserId;
	}

}
