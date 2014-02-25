package com.athlete.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.athlete.Constants;
import com.athlete.model.Comment;
import com.athlete.model.Conversation;
import com.athlete.model.ConversationM2MUser;
import com.athlete.model.FaceBookUser;
import com.athlete.model.FacebookUserM2M;
import com.athlete.model.Feed;
import com.athlete.model.Feed2Type2User;
import com.athlete.model.FriendsM2M;
import com.athlete.model.IdleWorkOut;
import com.athlete.model.Message;
import com.athlete.model.PostPicture;
import com.athlete.model.PreferenceUser;
import com.athlete.model.ProfileUser;
import com.athlete.model.Stats;
import com.athlete.model.User;
import com.athlete.model.UserM2MFeed;
import com.athlete.model.WorkOut;
import com.athlete.model.WorkoutM2MTrack;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * @author edBaev
 * */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String TAG = DatabaseHelper.class.getName();
	private static final String DB_NAME = "athlete.db";

	private static final int DB_VERSION = 2;
	private Context context;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;

	}

	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, WorkOut.class);
			TableUtils.createTable(connectionSource, Feed2Type2User.class);
			TableUtils.createTable(connectionSource, PostPicture.class);
			TableUtils.createTable(connectionSource, IdleWorkOut.class);
			TableUtils.createTable(connectionSource, Comment.class);
			TableUtils.createTable(connectionSource, ProfileUser.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, FacebookUserM2M.class);
			TableUtils.createTable(connectionSource, FriendsM2M.class);
			TableUtils.createTable(connectionSource, UserM2MFeed.class);
			TableUtils.createTable(connectionSource, PreferenceUser.class);
			TableUtils.createTable(connectionSource, Feed.class);
			TableUtils.createTable(connectionSource, Stats.class);

			TableUtils.createTable(connectionSource, WorkoutM2MTrack.class);
			TableUtils.createTable(connectionSource, ConversationM2MUser.class);
			TableUtils.createTable(connectionSource, Conversation.class);
			TableUtils.createTable(connectionSource, Message.class);
			TableUtils.createTable(connectionSource, FaceBookUser.class);

		} catch (Exception e) {
		
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
			ConnectionSource connectionSource, int oldVersion, int newVersion) {
		if (oldVersion == 0 && newVersion == 0) {
			clear(database, connectionSource);
		} else {
			// Add changes in old database
			if (oldVersion != newVersion) {
				logout();
				clear(database, connectionSource);

			}
		}
	}

	private void logout() {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.PREFERENCES, Context.MODE_PRIVATE);

		sp.edit().remove(Constants.SharedPreferencesKeys.USER_NAME).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.API_KEY).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.API_KEY).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.CURRENT_ID).commit();
		sp.edit().remove(Constants.INTENT_KEY.METRIC).commit();
		sp.edit().remove(Constants.INTENT_KEY.AUDIO_TIMING_METRIC).commit();
		sp.edit().remove(Constants.INTENT_KEY.AUDIO_TIMING_MILE).commit();
		sp.edit().remove(Constants.INTENT_KEY.COUNTDOUNT).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.FB_ACCES).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.NOTIFY_MSG).commit();
		sp.edit().remove(Constants.SharedPreferencesKeys.SERVER).commit();
	}

	private void clear(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			TableUtils.dropTable(connectionSource, Feed2Type2User.class, true);

			TableUtils.dropTable(connectionSource, Comment.class, true);
			TableUtils.dropTable(connectionSource, Feed.class, true);
			TableUtils.dropTable(connectionSource, PostPicture.class, true);
			TableUtils.dropTable(connectionSource, IdleWorkOut.class, true);
			TableUtils.dropTable(connectionSource, ProfileUser.class, true);
			TableUtils.dropTable(connectionSource, FacebookUserM2M.class, true);
			TableUtils.dropTable(connectionSource, PreferenceUser.class, true);
			TableUtils.dropTable(connectionSource, Stats.class, true);
			TableUtils.dropTable(connectionSource, WorkOut.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, FriendsM2M.class, true);
			TableUtils.dropTable(connectionSource, UserM2MFeed.class, true);
			TableUtils.dropTable(connectionSource, ConversationM2MUser.class,
					true);
			TableUtils.dropTable(connectionSource, FaceBookUser.class, true);
			TableUtils.dropTable(connectionSource, WorkoutM2MTrack.class, true);
			TableUtils.dropTable(connectionSource, Conversation.class, true);
			TableUtils.dropTable(connectionSource, Message.class, true);
			onCreate(database, connectionSource);
		} catch (Exception e) {
		
			throw new RuntimeException(e);
		}
	}
}