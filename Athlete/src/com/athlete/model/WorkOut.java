package com.athlete.model;

import java.io.Serializable;
import java.util.HashMap;

import com.athlete.Constants;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.WORKOUT)
public class WorkOut implements Serializable {
	private static final long serialVersionUID = 6269551655862822532L;
	/**
	 * @author edBaev
	 */

	public static final String ID = "id";
	public static final String DISTANCE = "distance_in_meters";
	public static final String DURATION = "duration_in_seconds";
	public static final String POST_BODY = "post_body";
	public static final String RUN_DATE = "run_date";

    public static final String ACTIVITY_TYPE = "activity_type";
    public static final String ACTIVITY_SUBTYPE = "activity_subtype";

	public static final String SOURCE = "source";
	public static final String STATIC_MAP_URL = "static_map_url";
	public static final String TITLE = "title";
	public static final String USER = "user";
	public static final String ROUTE = "route";
	public static final String IS_FAVORITE = "is_favorite";
	public static final String CALORIES = "calories";
	public static final String TYPE_ENDURANCE = "Endurance";
	public static final String TYPE_TEMPO = "Tempo";
	public static final String TYPE_SLOW = "Slow";
	public static final String TYPE_INTERVAL = "Interval";
	public static final String TYPE_GROUP = "Group";
	public static final String TYPE_RACE = "Race";
	public static final String TYPE_ELEVATION = "Elevation";
	public static final String FEED = "Feed";
	public static final String POST = "post";
	public static final String TRACK_ID = "track_id";
	public static final String ROUTE_ID = "route_id";

	@DatabaseField(columnName = FEED, foreign = true, foreignAutoRefresh = true)
	private Feed feed;
	@DatabaseField(id = true, columnName = ID)
	private int id;
	@DatabaseField(columnName = USER)
	private String idUser;
	@DatabaseField(columnName = DISTANCE)
	private double distance;
	@DatabaseField(columnName = DURATION)
	private long duration;
	@DatabaseField(columnName = CALORIES)
	private long calories;
	@DatabaseField(columnName = POST_BODY)
	private String postBody = "";
	@DatabaseField(columnName = RUN_DATE)
	private String runDate = "";
	@DatabaseField(columnName = IS_FAVORITE)
	private boolean favorite;

    @DatabaseField(columnName = ACTIVITY_TYPE)
	private String activityType = "";
    @DatabaseField(columnName = ACTIVITY_SUBTYPE)
    private String activitySubType = "";

	@DatabaseField(columnName = SOURCE)
	private String source = "";
	@DatabaseField(columnName = STATIC_MAP_URL)
	private String staticMapUrl = "";
	@DatabaseField(columnName = TITLE)
	private String title = "";
	@DatabaseField(columnName = POST)
	private String post;
	@DatabaseField(columnName = TRACK_ID)
	private long trackID;
	@DatabaseField(columnName = ROUTE_ID)
	private int routeID;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getPostBody() {
		return postBody;
	}

	public void setPostBody(String postBody) {
		this.postBody = postBody;
	}

	public String getRunDate() {
		return runDate;
	}

	public void setRunDate(String runDate) {
		this.runDate = runDate;
	}

    public String getActivityType() {
        return activityType;
    }

    public String getActivitySubType() {
        return activitySubType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setActivitySubType(String activitySubType) {
        this.activitySubType = activitySubType;
    }

    public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getStaticMapUrl() {
		return staticMapUrl;
	}

	public void setStaticMapUrl(String staticMapUrl) {
		this.staticMapUrl = staticMapUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdUser() {
		return idUser;
	}

	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}

	public long getCalories() {
		return calories;
	}

	public void setCalories(long calories) {
		this.calories = calories;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public long getTrackID() {
		return trackID;
	}

	public void setTrackID(long trackID) {
		this.trackID = trackID;
	}

	public int getRouteID() {
		return routeID;
	}

	public void setRouteID(int routeID) {
		this.routeID = routeID;
	}
}
