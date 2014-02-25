package com.athlete;

public abstract class Constants {
	/**
	 * @author edBaev
	 * */

	/**
	 * This is an abstract utility class.
	 */
	protected Constants() {
	}

	public static class MEDIA_PLAYER_KEY {
		public static final int REQUEST_CODE = 101;
		public static final String PROJECTION_KEY = "projection";
		public static final String SELECTION_KEY = "selection";
		public static final String POSITION_KEY = "position";
		public static final String WHERE_KEY = "where";
	}

	public static final boolean DEBUG = false;

	// ATHLETE
	public static final String APP_ID_FB = "165756903512701";

	public static final String FB_TAG = "facebook_";
	public static final String FB_PERMISSION_READ_STREAM = "read_stream";
	public static final String FB_PERMISSION_PUBLISH_ACTIONS = "publish_actions";
	public static final String FB_PERMISSION_USER_BIRTHDAY = "user_birthday";
	public static final String FB_PERMISSION_USER_PHOTOS = "user_photos";
	public static final String FB_PERMISSION_EMAIL = "email";
	public static final String FB_PERMISSION_LIKE = "user_likes";
	public static final String FB_PERMISSION_FR_LIKE = "friends_likes";
	public static final String FB_PERMISSION_FR_GROUPS = "friends_groups";
	public static final String FB_PERMISSION_FR_PHOTO = "friends_photos";
	public static final String FB_PERMISSION_PUBLISH_ACTION = "publish_actions";

	public static final String FB_PERMISSION_PUBLISH = "publish_stream";
	public static final String FB_REQUEST_ME = "me";
	public static final String PREFERENCES = "me";
	public static final int RESULT_CODE = 101;
	public static final int RESULT_CODE_TAB = 201;
	public static final int RESULT_CODE_TRACK = 103;
	public static final int RESULT_CODE_DELETE = 210;
	public static final int REQUEST_CODE = 102;
	public static final int REQUEST_CODE_MALE = 201;
	public static final int REQUEST_CODE_FEMALE = 200;
	public static final int REQUEST_CODE_WEIGHT = 202;
	public static final int REQUEST_CODE_BD = 203;
	public static final int REQUEST_CODE_METRIC = 204;
	public static final int REQUEST_CODE_TRANSFER = 206;
	public static final int REQUEST_CODE_COUNTDOUNT = 205;
	public static final int REQUEST_CODE_DELETE = 207;
	public static final int REQUEST_CODE_DURATION = 208;
	public static final int REQUEST_CODE_AUDIO_TIMING = 209;
	public static final int ANIM_DURATION = 700;
	public static final int ONE_SECOND = 1000;
	public static final double ONE_THSND = 1000.0;
	public static final int ONE_MILLION = 1000000;
	public static final String UNITS[] = new String[] { "mi", "km" };
	public static final String DOT = ".";
	public static final String REGULAR_EXPRESSION_LOG = "_";
	public static final String URL_TERMS = "http://www.athlete.com/legal/raw";
	public static final String URL_FORGOT_PASS = "https://www.athlete.com/accounts/forgot_password";
	public static final String URL_FEEDBACK = "http://athlete.uservoice.com/";
	public static final String UPDATE_PULL_DOWN = "update_pull_down";
	public static final String[] HOSTS_TIME = { "time.euro.apple.com",
			"time.apple.com" };
	public static final String[] PERMS = new String[] {
			FB_PERMISSION_FR_GROUPS, FB_PERMISSION_READ_STREAM,
			FB_PERMISSION_USER_BIRTHDAY, FB_PERMISSION_PUBLISH_ACTIONS,
			FB_PERMISSION_USER_PHOTOS, FB_PERMISSION_EMAIL, FB_PERMISSION_LIKE,
			FB_PERMISSION_FR_LIKE, FB_PERMISSION_FR_PHOTO,
			FB_PERMISSION_PUBLISH, FB_PERMISSION_PUBLISH_ACTION };

	public class WEIGHT_UNIT {

		public static final String POUNDS = "pounds";
		public static final String KILOGRAM = "kilograms";
	}

	public class HOST {
		public static final String HOST_MAIN = "https://www.athlete.com";
		public static final String HOST_STAGING = "https://staging.athlete.com";
		public static final String HOST_TEST = "https://test.athlete.com";
		public static final String HOST_DEFF = HOST_MAIN;
		public static final String API_V = "/api/v1";
        public static final String API_V2 = "/api/v2";
		public static final String SIGNATURE = "&signature=";
		public static final String TIMESTAMP = "&timestamp=";
		public static final String PUBLIC_KEY = "public_key=";
		public static final String APIKEY = "ApiKey";
	}

	public static final class TABLE_NAME {
		public static final String FEED2TYPE2USER = "Feed2Type2User";
		public static final String COMMENT = "Comment";
		public static final String FEED = "Feed";
		public static final String POSTPICTURE = "PostPicture";
		public static final String IDLE_WORKOUT = "IdleWorkOut";
		public static final String PROFILE_USER = "ProfileUser";
		public static final String PREFERENCE_USER = "PreferenceUser";
		public static final String STATS = "Stats";
		public static final String WORKOUT = "Workout";
		public static final String USER = "User";
		public static final String FB_USER = "FacebookUser";
		public static final String FB_USER_M2M = "FacebookUserM2M";
		public static final String FRIEND_M2M = "FriendM2M";
		public static final String USER_M2M_FEED = "UserM2MFeed";
		public static final String CONVERSATION_M2M_USER = "ConversationM2MUser";
		public static final String WORKOUT_M2M_TRACK = "WorkoutM2MTrack";
		public static final String PHOTO_M2M_TRACK = "PhotoM2MTrack";
		public static final String CONVERSATION = "Conversation";
		public static final String MESSAGE = "Message";
	}

	public static final class SharedPreferencesKeys {
		public static final String API_KEY = "api_key";
		public static final String USER_NAME = "user_name";
		public static final String CURRENT_ID = "id";
		public static final String PLAY_LIST_ID = "playListId";
		public static final String FB_ACCES = "fb_acces_token";
		public static final String AUTO_PAUSE = "auto_pause";
		public static final String NOTIFY_MSG = "notify_msg";
		public static final String SERVER = "server";
		public static final String URI = "uri_pref";
		public static final String FIRST_TIME = "first_time";
	}

	public static final class INTENT_KEY_FB {
		public static final String EMAIL_KEY = "email";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String GENDER = "gender";
		public static final String ID = "id";
	}

	public static final class OBJECT_TYPE {
		public static final String POST = "post";
		public static final String COMMENT = "comment";
	}

	public static final class AUDIO_TIMING_FLOAT {
		public static final float NEVER = -1;
		public static final float ZERO_POINT_FIVE = 0.5f;
		public static final float ONE = 1;
		public static final float TWO_POINT_FIVE = 2.5f;
	}

	public static final class INTENT_KEY {
		public static final String WORKOUT_ID = "workoutIdl";
		public static final String EMAIL = "email";
		public static final String NAME = "name";
		public static final String ID = "id";
		public static final String MUSIC = "music";
		public static final String FEED_ID = "feed_id";
		public static final String BOOLEAN_VALUE = "boolean_value";
		public static final String NOTIFICATION = "notification";
		public static final String WEIGHT = "weight";
		public static final String BD = "bd";
		public static final String METRIC = "metric";
		public static final String AUDIO_TIMING_METRIC = "audio_timing_metric";
		public static final String AUDIO_TIMING_MILE = "audio_timing_mile";
		public static final String UNIT = "unit";
		public static final String COUNTDOUNT = "countdount";
		public static final String USER_DETAILS = "user_details";
		public static final String USER_DETAILS_COUNT = "user_details_count";
		public static final String TIME = "time";
		public static final String TRACK_PATH = "track_path";
	}

	public static final class ERRORS {
		public static final String FB_EXISTS = "The Facebook account you provided already exists";
	}

	public static final class TYPE_FEED {
		public static final String FEATURED = "featured";
		public static final String FRIENDS = "friends";
		public static final String LOCAL = "local";
		public static final String PROFILE = "profile";
	}

	public class SIGNATURE {

		public static final String PUBLIC_KEY_MAIN = "VRn3e14ovkN9yX97YUeRUtju4wJ8Qf";
		public static final String PRIVATE_KEY_MAIN = "M1xugYdNlv1Rvyy0508fizZFV6NINk";

		public static final String POST = "POST\n";
		public static final String PATCH = "PATCH\n";
		public static final String GET = "GET\n";
		public static final String DELETE = "DELETE\n";
	}

	public class FORMATS {
		public static final String TWO_ZERO = "0.00";
	}

	public static final String TAG = "Athlete";

	/**
	 * Name of the top-level directory inside the SD card where our files will
	 * be read from/written to.
	 */
	public static final String SDCARD_TOP_DIR = "Athlete";

	/**
	 * The number of distance readings to smooth to get a stable signal.
	 */
	public static final int DISTANCE_SMOOTHING_FACTOR = 25;

	/**
	 * The number of elevation readings to smooth to get a somewhat accurate
	 * signal.
	 */
	public static final int ELEVATION_SMOOTHING_FACTOR = 25;

	/**
	 * The number of grade readings to smooth to get a somewhat accurate signal.
	 */
	public static final int GRADE_SMOOTHING_FACTOR = 5;

	/**
	 * The number of speed reading to smooth to get a somewhat accurate signal.
	 */
	public static final int SPEED_SMOOTHING_FACTOR = 25;

	/**
	 * Maximum number of track points displayed by the map overlay.
	 */
	public static final int MAX_DISPLAYED_TRACK_POINTS = 10000;

	/**
	 * Target number of track points displayed by the map overlay. We may
	 * display more than this number of points.
	 */
	public static final int TARGET_DISPLAYED_TRACK_POINTS = 5000;

	/**
	 * Maximum number of track points ever loaded at once from the provider into
	 * memory. With a recording frequency of 2 seconds, 15000 corresponds to 8.3
	 * hours.
	 */
	public static final double MARKER_Y_OFFSET_PERCENTAGE = 91 / 96.0;
	public static final int MAX_LOADED_TRACK_POINTS = 20000;

	/**
	 * Maximum number of track points ever loaded at once from the provider into
	 * memory in a single call to read points.
	 */
	public static final int MAX_LOADED_TRACK_POINTS_PER_BATCH = 1000;

	/**
	 * Maximum number of way points displayed by the map overlay.
	 */
	public static final int MAX_DISPLAYED_WAYPOINTS_POINTS = 128;

	/**
	 * Maximum number of way points that will be loaded at one time.
	 */
	public static final int MAX_LOADED_WAYPOINTS_POINTS = 10000;

	/**
	 * Any time segment where the distance traveled is less than this value will
	 * not be considered moving.
	 */
	public static final double MAX_NO_MOVEMENT_DISTANCE = 2;

	/**
	 * Anything faster than that (in meters per second) will be considered
	 * moving.
	 */
	public static final double MAX_NO_MOVEMENT_SPEED = 0.224;

	/**
	 * Ignore any acceleration faster than this. Will ignore any speeds that
	 * imply accelaration greater than 2g's 2g = 19.6 m/s^2 = 0.0002 m/ms^2 =
	 * 0.02 m/(m*ms)
	 */
	public static final double MAX_ACCELERATION = 0.02;

	/** Maximum age of a GPS location to be considered current. */
	public static final long MAX_LOCATION_AGE_MS = 60 * 1000; // 1 minute

	/** Maximum age of a network location to be considered current. */
	public static final long MAX_NETWORK_AGE_MS = 1000 * 60 * 10; // 10 minutes

	/**
	 * The type of account that we can use for gdata uploads.
	 */
	public static final String ACCOUNT_TYPE = "com.google";

	/**
	 * The name of extra intent property to indicate whether we want to resume a
	 * previously recorded track.
	 */
	public static final String RESUME_TRACK_EXTRA_NAME = "com.google.android.apps.mytracks.RESUME_TRACK";

	public static final String MAPSHOP_BASE_URL = "https://maps.google.com/maps/ms";

	/*
	 * Default values - keep in sync with those in preferences.xml.
	 */

	public static final int DEFAULT_ANNOUNCEMENT_FREQUENCY = -1;
	public static final int DEFAULT_AUTO_RESUME_TRACK_TIMEOUT = 10; // In min.
	public static final int DEFAULT_MAX_RECORDING_DISTANCE = 200;
	public static final int DEFAULT_MIN_RECORDING_DISTANCE = 5;
	public static final int DEFAULT_MIN_RECORDING_INTERVAL = 0;
	public static final int DEFAULT_SPLIT_FREQUENCY = 0;

	public static final String SETTINGS_NAME = "SettingsActivity";
}
