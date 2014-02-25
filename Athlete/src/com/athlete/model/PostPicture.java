package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.POSTPICTURE)
public class PostPicture implements Serializable {

	private static final long serialVersionUID = -4881167789279107381L;
	/**
	 * @author edBaev
	 */

	public static final String ID = "id";
	public static final String FEED_ID = "feedId";
	public static final String DETAIL = "detail";
	public static final String FEED = "feed";
	public static final String ORIGINAL = "original";
	public static final String THUMBNAIL = "thumbnail";
	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = FEED_ID)
	private int feedId;
	@DatabaseField(columnName = DETAIL)
	private String detail;
	@DatabaseField(columnName = FEED)
	private String feed;
	@DatabaseField(columnName = ORIGINAL)
	private String original;
	@DatabaseField(columnName = THUMBNAIL)
	private String thumbnail;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFeedId() {
		return feedId;
	}

	public void setFeedId(int feedId) {
		this.feedId = feedId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getFeed() {
		return feed;
	}

	public void setFeed(String feed) {
		this.feed = feed;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

}
