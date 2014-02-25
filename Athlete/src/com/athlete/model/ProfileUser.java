package com.athlete.model;

import java.io.Serializable;

import com.athlete.Constants;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Constants.TABLE_NAME.PROFILE_USER)
public class ProfileUser implements Serializable {

	private static final long serialVersionUID = -8623162662508283071L;

	/**
	 * @author edBaev
	 */

	/**
	 * { "birth_date": "1983-05-17", "gender": "M", "id": 1, "location": { "id":
	 * 1, "lat": "40.848556", "lng": "-111.906882", "name":
	 * "North Salt Lake, UT", "resource_uri": "" }, "resource_uri":
	 * "/api/v1/profile/1/", "user": "/api/v1/user/1/", "weight": "155.00",
	 * "weight_unit": "pounds" }
	 */
	public static final String ID = "id";

	public static final String GENDER = "gender";
	public static final String USER = "user";
	public static final String USERS = "users";
	public static final String WEIGHT = "weight";
	public static final String BIRTH_DATE = "birth_date";
	public static final String WEIGHT_UNIT = "weight_unit";
	
	public static final String LOCATION = "location";
	public static final String LAT = "lat";
	public static final String LNG = "lng";
	public static final String NAME = "name";
	
	@DatabaseField(id = true, columnName = ID)
	private String id;

	@DatabaseField(columnName = WEIGHT_UNIT)
	private String weightUnit;

	@DatabaseField(columnName = BIRTH_DATE)
	private String birthDate;

	@DatabaseField(columnName = GENDER)
	private String gender;
	@DatabaseField(columnName = NAME)
	private String locationName;
	@DatabaseField(columnName = WEIGHT)
	private double weight;
	@DatabaseField(columnName = LAT)
	private double lat;
	@DatabaseField(columnName = LNG)
	private double lng;
	
	@DatabaseField(columnName = USER, foreign = true, foreignAutoRefresh = true)
	private User user;
	@ForeignCollectionField(columnName = USERS)
	private ForeignCollection<User> users;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getWeightUnit() {
		return weightUnit;
	}

	public void setWeightUnit(String weightUnit) {
		this.weightUnit = weightUnit;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

}
