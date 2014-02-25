package com.athlete.util;

import android.content.Context;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class AnalyticsUtils {
	// realise
	private static final String UA = "UA-33381112-2";
	// debug
	// private static final String UA = "UA-36819867-1";
	private static final String PRODUCT_NAME = "android-athlete";

	private AnalyticsUtils() {
	}

	/**
	 * Sends a page view.
	 * 
	 * @param context
	 *            the context
	 * @param page
	 *            the page
	 */
	public static void sendPageViews(Context context, String page) {
		GoogleAnalytics myInstance = GoogleAnalytics.getInstance(context);
		Tracker tracker = myInstance.getTracker(UA);
		tracker.setAppName(PRODUCT_NAME);
		tracker.trackView(page);
		myInstance.closeTracker(tracker);
	}

	public class GOOGLE_ANALYTICS {

		public static final String CATEGORY = "Clicks";
		public static final String ACTION = "button";
		public static final String COMMENT = "comment";
		public static final String COMPOSE = "compose";
		public static final String LIKE = "like";
		public static final String INVITE_SUGG_FB = "invite_suggested_fb";
		public static final String INVITE_FB = "invite_fb";
		public static final String INVITE_EMAIL = "invite_email";
		public static final String LOGIN_EMAIL = "login_email";
		public static final String SIGNUP_EMAIL = "signup_email";
		public static final String SIGNUP_FB = "signup_fb";
		public static final String LOGIN_FB = "login_fb";

		public static final String START_TRACK = "start_track";
		public static final String RESUME_TRACK = "resume_track";
		public static final String PAUSE_TRACK = "pause_track";

		public static final String ADD_USER_TO_FRIEND = "add user to friend";
		public static final String REMOVE_USER_FROM_FRIEND = "remove user from friend";
		public static final String APPROVE_THE_REQ = "approve the request";
		public static final String REMOVE_THE_MINE_REQ = "remove the mine the request";
		public static final String CANCEL_FRIEND_REQ = "cancel friendship request";

		public class SCREEN {
			public static final String TUTORIAL = "TutorialScreen";
			public static final String SIGNUP = "SignUpScreen";
			public static final String LOGIN = "LogInScreen";
			public static final String SIGNUP_DETAILS = "SignUpDetailsScreen";
			public static final String SELECT_DB = "SelectDateOfBirthScreen";
			public static final String SELECT_GENDER = "SelectGenderScreen";
			public static final String TERMS = "TermsOfUseScreen";
			public static final String SELECT_WEIGHT = "SelectWeightScreen";
			public static final String WHY = "SignUpDetailsScreen/WhyCompleteThisScreen";
			public static final String FEED_FEATURE = "FeedListScreen/Featured";
			public static final String FEED_FRIENDS = "FeedListScreen/Friends";
			public static final String FEED_LOCAL = "FeedListScreen/Local";
			public static final String FEED_SEARCH = "FeedListScreen/Search";
			public static final String LOG = "LogListScreen";
			public static final String CONVERSATION_LIST = "ConversationListScreen";
			public static final String CONVERSATION = "ConversationScreen";
			public static final String CONVERSATION_CREATE = "CreateConversationScreen";
			public static final String SELECT_AUDIO_TIMING = "SelectAudioTimingScreen";
			public static final String SELECT_COUNTDOWN = "SelectCountdownScreen";
			public static final String SELECT_LOCATION = "SelectLocationScreen";
			public static final String SELECT_METRIC = "SelectMetricScreen";
			public static final String MANUAL = "ManualScreen";
			public static final String ROUTE = "RouteScreen";
			public static final String SPLIT = "SplitScreen";
			public static final String SETUP = "SetupScreen";
			public static final String MESSAGE_SCREEN = "MessagesScreen";
			public static final String TRACK_SCREEN = "TrackScreen";

			public static final String ROUTE_DETAILS = "RouteDetailScreen";
			public static final String SPLIT_DETAILS = "SplitDetailScreen";
			public static final String FEED_DETAILS = "FeedDetailScreen";
			public static final String MUSIC = "MusicScreen";
			public static final String FRIEND = "FriendsScreen";
			public static final String FIND_FRIEND_SEARCH = "FindFriendsScreen/Search";
			public static final String FRIEND_INVITE_CONTACT = "FriendsInviteContactScreen";
			public static final String FIND_FRIEND = "FindFriendsScreen";

			public static final String FB_FRIEND = "FacebookFriendsScreen";
			public static final String PROFILE = "ProfileScreen";
			public static final String SAVE = "SaveScreen";
		}
	}

	public static void sendPageViews(Context context, String page,
			String category, String action, String label, long value) {
		GoogleAnalytics myInstance = GoogleAnalytics.getInstance(context);
		Tracker tracker = myInstance.getTracker(UA);
		tracker.setAppName(PRODUCT_NAME);

		tracker.trackEvent(category, action, label, value);
		tracker.trackView(page);
		myInstance.closeTracker(tracker);
	}
}