package com.athlete.activity.user;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.TabActivityMain;
import com.athlete.activity.msg.ActivityNewMessage;
import com.athlete.activity.track.details.BaseTrackDetails;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.bl.FriendsBL;
import com.athlete.control.RoundedImageView;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.ConversationM2MUser;
import com.athlete.model.Feed;
import com.athlete.model.Feed2Type2User;
import com.athlete.model.FriendsM2M;
import com.athlete.model.PreferenceUser;
import com.athlete.model.ProfileUser;
import com.athlete.model.Stats;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.model.WorkOut;
import com.athlete.services.BaseTask;
import com.athlete.services.IAppFilter;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.AcceptFriendRequestTask;
import com.athlete.services.task.RequestFriendTask;
import com.athlete.services.task.delete.DeleteFriendshipTask;
import com.athlete.services.task.get.GetFeedTask;
import com.athlete.services.task.get.GetFriendExistTask;
import com.athlete.services.task.get.GetFriendsTask;
import com.athlete.services.task.get.GetProfileTask;
import com.athlete.services.task.get.GetStatsTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

public class ActivityUserDetails extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private ImageView imageLoading;
	private ImageButton mImViewUserPlus, mImViewCommentPlus,
			mImViewCancelRequest;
	private TextView mTxtFullName, mTxtArea;
	private User userDetails;
	private View header;
	private ListView listView;
	private List<Feed> listOfFeed;
	private BaseListAdapter<Feed> adapter;
	private boolean isAllDialogsLoaded;
	private Animation animLarge;
	private ArrayList<User> friends;
	private TableRow.LayoutParams paramsAvaForward;
	private Stats stats;
	private TableRow tableRowDots;
	private int countFriend;
	private TableRow.LayoutParams layoutParams;
	private ProfileUser profileUser;
	private List<FriendsM2M> friendsM2Ms;
	private FriendsM2M friendsM2M;
	// 1-not friend, 2-isFriend, 3- friend's request 4- mine request
	private int type;
	private User currentUser;
	private final int maxDotes = 30;
	private final int typeNotFriend = 1;
	private final int typeIsFriend = 2;
	private final int typeFrndRqst = 3;
	private final int typeMineRqst = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.actv_user_details);
		String userId = (String) getIntent().getStringExtra(
				Constants.INTENT_KEY.USER_DETAILS);
		userDetails = userBL.getBy(userId);

		friendsBL = new FriendsBL(getHelper());

		if (userDetails != null) {
			userNotNull();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityUserDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.PROFILE);
	}

	private void userNotNull() {
		profileUser = baseBl.getFromDBByField(ProfileUser.class,
				ProfileUser.ID, userDetails.getProfileID());
		if (profileUser == null) {
			profileUser = new ProfileUser();
		}
		if (profileUser.getId() == null) {
			profileUser.setId(userDetails.getProfileID());
		}
		setView();
		currentUser = userBL.getBy(mId);
		setArea();
		updateTheFeed(null, true);
		updateTheFriend();
		updateTheStats();
		updateTheUserProfile();
		checkFriendShip();
	}

	private void setView() {
		imageLoading = (ImageView) findViewById(R.id.imVLoading);
		sp = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);
		friendsM2Ms = friendsBL.getFriendM2M(mId, userDetails.getId());

		paramsAvaForward = new TableRow.LayoutParams(size35dp, size35dp);
		paramsAvaForward.setMargins(padding2DP, 0, 0, 0);
		layoutParams = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(padding2DP, 0, 0, 0);
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
		listOfFeed = new LinkedList<Feed>();
		listOfFeed = user2FeedBl.getFeedByUser(userDetails.getId());

		friends = new ArrayList<User>();
		mImViewUserPlus = (ImageButton) findViewById(R.id.imViewUserPlus);
		mImViewCancelRequest = (ImageButton) findViewById(R.id.imViewCancelRequest);
		mImViewCancelRequest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelFriendshipRequest();
			}
		});
		mImViewCommentPlus = (ImageButton) findViewById(R.id.imViewCommentPlus);
		checkIsFriend();
		header = getLayoutInflater()
				.inflate(R.layout.header_user_details, null);
		tableRowDots = (TableRow) header.findViewById(R.id.tableRowDots);
		mTxtFullName = (TextView) header.findViewById(R.id.txtFullName);
		RoundedImageView roundImageAva = (RoundedImageView) header
				.findViewById(R.id.roundedImageAva);
		roundImageAva.setCornerRadius(corner2DP, size105dp);
		imageLoader.displayImage(userDetails.getProfileImage225url(),
				roundImageAva, options);

		TextView mTitle = (TextView) findViewById(R.id.txtUserName);
		mTxtArea = (TextView) header.findViewById(R.id.txtArea);
		mTitle.setText(userDetails.getFirstName() + " "
				+ userDetails.getLastName());
		mTxtFullName.setText(userDetails.getFirstName() + " "
				+ userDetails.getLastName());

		listView = (ListView) findViewById(R.id.listView);
		listView.addHeaderView(header);

		if (userDetails.getId().equalsIgnoreCase(mId)) {
			mImViewUserPlus.setVisibility(View.GONE);
			mImViewCommentPlus.setVisibility(View.GONE);
		}
		mImViewUserPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setOnClickUser();
			}
		});
		mImViewCommentPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ActivityUserDetails.this,
						ActivityNewMessage.class).putExtra(
						Constants.INTENT_KEY.USER_DETAILS, userDetails.getId()));
			}
		});
		stats = statsBL.getStatsByUser(userDetails.getId());
		if (stats != null) {
			setViewStats();
		}
		getFriends();
		setAdapter();
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnRefresh = (ImageButton) findViewById(R.id.btnResresh);
		mBtnRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				updateTheFeed(null, true);
				updateTheFriend();
				updateTheStats();
				checkFriendShip();
			}
		});
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();

			}
		});

	}

	private void checkFriendShip() {
		showProgress();
		OnTskCpltListener getFriendExist = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (!result.isError()) {
						friendsM2Ms = friendsBL.getFriendM2M(mId,
								userDetails.getId());

					} else {
						if (result.getError().equals("404")) {
							friendsM2Ms = null;
							mImViewCancelRequest.setVisibility(View.GONE);
						}
					}
					checkIsFriend();
				} catch (Exception e) {

				}
			}
		};
		GetFriendExistTask getFriendExistTask = new GetFriendExistTask(
				ActivityUserDetails.this, baseBl, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), currentUser,
				userDetails.getId());
		getTaskManager().executeTask(getFriendExistTask, getFriendExist, null,
				true);
	}

	private void checkIsFriend() {
		if (friendsM2Ms != null && !friendsM2Ms.isEmpty()) {
			friendsM2M = friendsM2Ms.get(0);
			// Accepted
			if (friendsM2M.getStatus().equalsIgnoreCase(FriendsM2M.ACCEPTED)) {
				type = typeIsFriend;
			} else {
				// Pending
				if (friendsM2M.getRequester() == Integer.valueOf(mId)) {
					type = typeMineRqst;
				} else {
					type = typeFrndRqst;
				}
			}
		} else {
			type = typeNotFriend;
		}
		setImageFriend();
	}

	private void setImageFriend() {
		switch (type) {
		case 1:
			mImViewUserPlus.setImageResource(R.drawable.icon_userplus3);
			break;
		case 2:
			mImViewUserPlus.setImageResource(R.drawable.icon_userno);
			break;
		case 3:
			mImViewUserPlus.setImageResource(R.drawable.icon_useryes);
			mImViewCancelRequest.setVisibility(View.VISIBLE);
			break;
		case 4:
			mImViewUserPlus.setImageResource(R.drawable.icon_userquestion);
			break;
		default:
			mImViewUserPlus.setImageResource(R.drawable.icon_userplus3);
			break;
		}
	}

	private void setOnClickUser() {
		switch (type) {
		case 1:

			AnalyticsUtils.sendPageViews(ActivityUserDetails.this,

			AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.PROFILE,
					AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
					AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
					AnalyticsUtils.GOOGLE_ANALYTICS.ADD_USER_TO_FRIEND, 0);
			addUser();
			break;
		case 2:
			AnalyticsUtils.sendPageViews(ActivityUserDetails.this,
					AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.PROFILE,
					AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
					AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
					AnalyticsUtils.GOOGLE_ANALYTICS.REMOVE_USER_FROM_FRIEND, 0);
			removeUserFromFriends();

			break;
		case 3:
			AnalyticsUtils.sendPageViews(ActivityUserDetails.this,
					AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.PROFILE,
					AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
					AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
					AnalyticsUtils.GOOGLE_ANALYTICS.APPROVE_THE_REQ, 0);
			approveTheRequest();
			break;
		case 4:
			AnalyticsUtils.sendPageViews(ActivityUserDetails.this,
					AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.PROFILE,
					AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
					AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
					AnalyticsUtils.GOOGLE_ANALYTICS.REMOVE_THE_MINE_REQ, 0);
			removeTheMineRequest();
			break;

		}
	}

	private void addUser() {
		requestFriendship();
	}

	private void removeUserFromFriends() {
		alertDialogCancelRequest(getString(R.string.message_delete_from_friend)
				+ " " + userDetails.getFirstName() + " "
				+ userDetails.getLastName() + " "
				+ getString(R.string.message_delete_from_friend_second));
	}

	private void approveTheRequest() {
		acceptFriendshipRequest();
	}

	private void removeTheMineRequest() {
		alertDialogCancelRequest(getString(R.string.message_cancel_request));
	}

	private void setArea() {
		if (profileUser != null && profileUser.getLocationName() != null
				&& profileUser.getLocationName().length() > 0) {
			mTxtArea.setText(profileUser.getLocationName());
		}
	}

	private void acceptFriendshipRequest() {
		showProgress();
		OnTskCpltListener acceptFriendRequest = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.getResult()) {
						friendsM2M.setStatus(FriendsM2M.ACCEPTED);
						baseBl.createOrUpdate(FriendsM2M.class, friendsM2M);
						mImViewCancelRequest.setVisibility(View.GONE);
						checkIsFriend();
						updateTheFriend();
						setFriendsAvatar();
					}
				} catch (Exception e) {
				}
			}
		};
		AcceptFriendRequestTask acceptFriendRequestTask = new AcceptFriendRequestTask(
				ActivityUserDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(),
				friendsM2M.getFriendShipId());
		getTaskManager().executeTask(acceptFriendRequestTask,
				acceptFriendRequest, null, true);
	}

	private void requestFriendship() {
		showProgress();
		OnTskCpltListener requestFriend = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				TaskResult<String> result;
				try {
					result = (TaskResult<String>) task.get();
					if (result.getResult().length() > 0) {
						FriendsM2M friendsM2M = new FriendsM2M(
								userBL.getBy(mId), userDetails, 0,
								Integer.valueOf(mId), FriendsM2M.PENDING,
								result.getResult());
						baseBl.createOrUpdate(FriendsM2M.class, friendsM2M);
						friendsM2Ms = new LinkedList<FriendsM2M>();
						friendsM2Ms.add(friendsM2M);
						checkIsFriend();
					}
				} catch (Exception e) {
				}
			}
		};
		RequestFriendTask requestFriendTask = new RequestFriendTask(
				ActivityUserDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(),
				userDetails.getId());
		getTaskManager().executeTask(requestFriendTask, requestFriend, null,
				true);
	}

	private void cancelFriendshipRequest() {
		PreferencesUtils.setBoolean(ActivityUserDetails.this,
				R.string.is_delete_friend, true);
		AnalyticsUtils.sendPageViews(ActivityUserDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.PROFILE,
				AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
				AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
				AnalyticsUtils.GOOGLE_ANALYTICS.CANCEL_FRIEND_REQ, 0);
		showProgress();
		OnTskCpltListener deleteFriendship = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.getResult()) {
						baseBl.delete(friendsM2M, FriendsM2M.class);
						friendsM2Ms = null;
						mImViewCancelRequest.setVisibility(View.GONE);
						List<User> pending = friendsBL
								.getFriendByUserPending(mId);

						if (pending != null) {
							PreferencesUtils.setString(
									ActivityUserDetails.this, R.string.friends,
									String.valueOf(pending.size()));
						}
						checkIsFriend();
						updateTheFriend();
						setFriendsAvatar();
					}
				} catch (Exception e) {
				}
			}
		};
		DeleteFriendshipTask deleteFriendshipTask = new DeleteFriendshipTask(
				ActivityUserDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(),
				friendsM2M.getFriendShipId());
		getTaskManager().executeTask(deleteFriendshipTask, deleteFriendship,
				null, true);
	}

	private void showProgress() {
		findViewById(R.id.relativeCircle).setVisibility(View.VISIBLE);
		mBtnRefresh.setVisibility(View.GONE);
		imageLoading.startAnimation(anim);
	}

	private void hideProgress() {
		imageLoading.clearAnimation();
		mBtnRefresh.setVisibility(View.VISIBLE);
		findViewById(R.id.relativeCircle).setVisibility(View.GONE);
	}

	private void updateTheFeed(String laterThan, final boolean update) {
		showProgress();
		OnTskCpltListener getFeed = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<List<Feed>> result;
				hideProgress();

				try {
					result = (TaskResult<List<Feed>>) task.get();

					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (result.getError_description().equals("404")) {
							PreferencesUtils.setBoolean(
									ActivityUserDetails.this,
									R.string.is_delete_local, true);
							PreferencesUtils.setBoolean(
									ActivityUserDetails.this,
									R.string.is_delete_featured, true);
							PreferencesUtils.setBoolean(
									ActivityUserDetails.this,
									R.string.is_delete_friend, true);
							baseBl.deleteByField(WorkOut.USER, WorkOut.class,
									userDetails.getId());
							baseBl.deleteByField(Feed.AUTHOR, Feed.class,
									userDetails);

							baseBl.deleteByField(ConversationM2MUser.USER,
									ConversationM2MUser.class, userDetails);
							baseBl.deleteByField(Stats.USER, Stats.class,
									userDetails);
							baseBl.deleteByField(ProfileUser.USER,
									ProfileUser.class, userDetails);
							baseBl.deleteByField(PreferenceUser.USER,
									PreferenceUser.class, userDetails);
							baseBl.deleteByField(Feed2Type2User.USER,
									Feed2Type2User.class, userDetails.getId());
							userBL.delete(userDetails, User.class);
							Toast.makeText(ActivityUserDetails.this,
									getString(R.string.user_not_exist),
									Toast.LENGTH_SHORT).show();
							finish();
							startActivity(new Intent(ActivityUserDetails.this,
									TabActivityMain.class)
									.putExtra(
											Constants.INTENT_KEY.BOOLEAN_VALUE,
											true)
									.setFlags(
											Intent.FLAG_ACTIVITY_CLEAR_TOP
													| Intent.FLAG_ACTIVITY_SINGLE_TOP));
						}
						if (adapter != null)
							adapter.notifyDataSetChanged();
					} else {
						if (result.getResult().size() > 0) {
							if (update)
								listOfFeed.clear();
							listOfFeed.addAll(result.getResult());

							if (result.getResult().size() == 20)
								isAllDialogsLoaded = false;
							else
								isAllDialogsLoaded = true;
							Parcelable state = listView.onSaveInstanceState();
							setAdapter();
							listView.onRestoreInstanceState(state);
						}
					}
				} catch (Exception e) {
				}
			}
		};

		GetFeedTask feedTask = new GetFeedTask(ActivityUserDetails.this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, Constants.TYPE_FEED.PROFILE, userDetails.getId(),
				null, laterThan, null, Integer.valueOf(mId), 0, false,0);
		getTaskManager().executeTask(feedTask, getFeed, null, true);
	}

	private void updateTheFriend() {
		OnTskCpltListener getFeed = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<ArrayList<User>> result;
				try {
					result = (TaskResult<ArrayList<User>>) task.get();
					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					} else {
						if (result.getResult().size() > 0) {
							friends = result.getResult();
							getFriends();
						}
					}
				} catch (Exception e) {
				}
			}
		};

		GetFriendsTask friendTask = new GetFriendsTask(
				ActivityUserDetails.this, baseBl, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, userDetails, null, null);
		getTaskManager().executeTask(friendTask, getFeed, null, true);
	}

	private void updateTheStats() {
		OnTskCpltListener getStats = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<Stats> result;
				try {
					result = (TaskResult<Stats>) task.get();

					if (!result.isError() && result.getResult() != null) {
						stats = result.getResult();
					}
					setViewStats();
				} catch (Exception e) {
				}
			}
		};

		GetStatsTask statsTask = new GetStatsTask(ActivityUserDetails.this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, userDetails);
		getTaskManager().executeTask(statsTask, getStats, null, true);
	}

	private void updateTheUserProfile() {
		OnTskCpltListener getPreference = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<ProfileUser> result;
				try {
					result = (TaskResult<ProfileUser>) task.get();

					if (!result.isError() && result.getResult() != null) {
						profileUser = result.getResult();
						setArea();
					}
					setViewStats();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		};

		GetProfileTask profileTask = new GetProfileTask(
				ActivityUserDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, userDetails, null);
		getTaskManager().executeTask(profileTask, getPreference, null, true);
	}

	private void getFriends() {
		friends = new ArrayList<User>();
		friends = friendsBL
				.getFriendByUser(String.valueOf(userDetails.getId()));

		countFriend = friendsBL.getCount();
		setFriendsAvatar();

	}

	private void setViewStats() {
		tableRowDots.removeAllViews();
		int countDots = (int) (Math.round(stats.getAvgWeekly()
				* CommonHelper.getK(isMetric())));
		int fullDots = countDots / 3;
		int divDots = countDots % 3;
		int totalFor = fullDots;
		if (divDots > 0)
			totalFor++;
		for (int i = 0; i < totalFor; i++) {
			ImageView im = new ImageView(ActivityUserDetails.this);
			im.setLayoutParams(layoutParams);
			if (fullDots != 0) {
				im.setImageResource(R.drawable.volume_active);
				fullDots--;
			} else if (divDots == 1) {
				im.setImageResource(R.drawable.volume_active1);
			} else if (divDots == 2) {
				im.setImageResource(R.drawable.volume_active2);
			}
			tableRowDots.addView(im);
		}
		for (int i = 0; i < maxDotes - totalFor; i++) {
			ImageView im = new ImageView(ActivityUserDetails.this);
			im.setLayoutParams(layoutParams);
			im.setImageResource(R.drawable.volume_background);
			tableRowDots.addView(im);
		}
		int countMilesByWeek = CommonHelper.convertMetersToMilesAVG(
				stats.getAvgWeekly(), isMetric());
		TextView txtTime = (TextView) header.findViewById(R.id.txtTime);

		txtTime.setText(Html.fromHtml("<font color='#54A1C7'>"
				+ countMilesByWeek + "</font>"
				+ CommonHelper.getMiOrKm(isMetric())
				+ getString(R.string.header_list_user_details_mi_wk)));
	}

	private void setFriendsAvatar() {
		TableRow tableRow = (TableRow) header.findViewById(R.id.tableRowAva);
		tableRow.removeViews(0, tableRow.getChildCount() - 1);
		Button defAva = (Button) header.findViewById(R.id.defAva);
		defAva.setText(String.valueOf(countFriend));
		defAva.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (countFriend != 0) {
					startActivity(new Intent(ActivityUserDetails.this,
							ActivityFriend.class).putExtra(
							Constants.INTENT_KEY.USER_DETAILS, userDetails)
							.putExtra(Constants.INTENT_KEY.USER_DETAILS_COUNT,
									String.valueOf(countFriend)));
				}

			}
		});
		for (final User user : friends) {
			RoundedImageView roundedImageView = new RoundedImageView(
					ActivityUserDetails.this);

			roundedImageView.setCornerRadius(corner2DP, size35dp);
			roundedImageView.setLayoutParams(paramsAvaForward);
			roundedImageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View paramView) {
					startActivity(new Intent(ActivityUserDetails.this,
							ActivityUserDetails.class).putExtra(
							Constants.INTENT_KEY.USER_DETAILS, user.getId()));

				}
			});
			if (user.getProfileImage225url().startsWith("http"))
				imageLoader.displayImage(user.getProfileImage225url(),
						roundedImageView, options);
			else
				roundedImageView.setImageResource(R.drawable.avatar);

			tableRow.addView(roundedImageView, tableRow.getChildCount() - 1);
			if (tableRow.getChildCount() == 8)
				break;
		}
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<Feed>(this, listOfFeed,
				R.layout.item_feed, new IAppFilter<Feed>() {

					@Override
					public boolean performFiltering(CharSequence constraint,
							Feed item) {

						return false;
					}
				}) {
			@Override
			public int getCount() {
				return super.getCount()
						+ ((!isAllDialogsLoaded && !adapter.isFilter()) ? 1 : 0);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final Feed item = getItem(position);
				if (item == null)
					return new View(ActivityUserDetails.this);
				if (position == getCount() - 1 && !isAllDialogsLoaded
						&& !adapter.isFilter()) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.row_update_footer,
							null);
					view.setId(R.layout.row_update_footer);
					View splash = view.findViewById(R.id.imVLoading);
					splash.startAnimation(animLarge);
					updateTheFeed(
							(String.valueOf(getItem(position - 1).getId())),
							false);
					return view;
				}

				if (convertView == null
						|| convertView.getId() != R.layout.item_feed) {
					holder = new ViewHolder();
					convertView = getLayout();
					convertView.setId(R.layout.item_feed);
					holder.txtBody = (TextView) convertView
							.findViewById(R.id.txtBody);
					holder.txtName = (TextView) convertView
							.findViewById(R.id.txtFullName);
					holder.imAva = (RoundedImageView) convertView
							.findViewById(R.id.imViewAva);
					holder.imAva.setCornerRadius(corner2DP, size40dp);
					holder.txtCommentCount = (TextView) convertView
							.findViewById(R.id.txtComment);
					holder.txtLikeCount = (TextView) convertView
							.findViewById(R.id.txtLike);
					holder.txtTime = (TextView) convertView
							.findViewById(R.id.txtTime);
					holder.txtDistance = (TextView) convertView
							.findViewById(R.id.txtDistantionTime);
					holder.txtDuration = (TextView) convertView
							.findViewById(R.id.txtMeanValue);
					holder.txtTitle = (TextView) convertView
							.findViewById(R.id.txtTitle);
					holder.imType = (TextView) convertView
							.findViewById(R.id.txtActivityType);
					convertView.setTag(holder);
				} else
					holder = (ViewHolder) convertView.getTag();

				if (item.getBody() != null)
					holder.txtBody.setText(item.getBody());
				holder.txtName.setText(item.getUser().getFirstName() + " "
						+ item.getUser().getLastName());
				// distance
				if (item.getWorkOut() != null) {
					holder.imType.setVisibility(View.VISIBLE);
					holder.txtDistance.setText(String.valueOf(CommonHelper
							.convertMetersToMiles(item.getWorkOut()
									.getDistance(), isMetric()))
							+ CommonHelper.getMiOrKm(isMetric())
							+ CommonHelper.getDateMSS(item.getWorkOut()
									.getDuration(), ActivityUserDetails.this));
					holder.txtDuration
							.setText("("
									+ CommonHelper
											.getDateMSSQuote(
													(long) (item.getWorkOut()
															.getDuration() / (item
															.getWorkOut()
															.getDistance() * CommonHelper
															.getK(isMetric()))),
													ActivityUserDetails.this)
									+ ")");
					holder.txtTitle.setText(item.getWorkOut().getTitle());

                    Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/athlete-regular-webfont.ttf");
                    holder.imType.setTypeface(typeFace);
					CommonHelper.setType(holder.imType, item.getWorkOut(), null, getResources());

					convertView.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							startActivity(new Intent(ActivityUserDetails.this,
									BaseTrackDetails.class).putExtra(
									Constants.INTENT_KEY.ID, item.getWorkOut()
											.getId()));
						}
					});

				} else {
					holder.txtDistance.setText("");
					holder.txtTitle.setText("");
					holder.txtDuration.setText("");
					holder.imType.setVisibility(View.GONE);
				}
				if (item.getUser().getProfileImage225url().startsWith("http"))
					imageLoader.displayImage(item.getUser()
							.getProfileImage225url(), holder.imAva, options);
				else
					holder.imAva.setImageResource(R.drawable.avatar);

				// time
				holder.txtTime.setText(CommonHelper.getLastSeen(CommonHelper
						.getLongYYYYMMDDtHHMMSS(item.getDisplayDate()),
						ActivityUserDetails.this));
				// comment
				if (item.getCommentCount() > 0) {

					if (item.getCommentCount() > 1)
						holder.txtCommentCount.setText(Constants.DOT
								+ item.getCommentCount() + " "
								+ getString(R.string.txt_comments));
					else
						holder.txtCommentCount.setText(Constants.DOT
								+ item.getCommentCount() + " "
								+ getString(R.string.txt_comment));
				} else
					holder.txtCommentCount.setText("");
				// like
				if (item.getLikers().size() > 0) {
					if (item.getLikers().size() > 1)
						holder.txtLikeCount.setText(Constants.DOT
								+ item.getLikers().size() + " "
								+ getString(R.string.txt_likes));
					else
						holder.txtLikeCount.setText(Constants.DOT
								+ item.getLikers().size() + " "
								+ getString(R.string.txt_like));
				} else
					holder.txtLikeCount.setText("");

				return convertView;

			}
		};
		listView.setAdapter(adapter);
	}

	private void alertDialogCancelRequest(String str) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_discard))
				.setMessage(str)
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								cancelFriendshipRequest();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}
}
