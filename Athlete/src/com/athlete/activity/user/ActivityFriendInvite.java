package com.athlete.activity.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.SectionFriendInviteAdapter;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.FriendsBL;
import com.athlete.control.RoundedImageView;
import com.athlete.control.SessionEvents;
import com.athlete.control.SessionStore;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.FaceBookUser;
import com.athlete.model.FacebookUserM2M;
import com.athlete.model.FriendsM2M;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.parser.FaceBookUserParser;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.AcceptFriendRequestTask;
import com.athlete.services.task.SendInviteByEmailTask;
import com.athlete.services.task.delete.DeleteFriendshipTask;
import com.athlete.services.task.get.GetFriendsTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.BaseRequestListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.segment.SegmentListView;

public class ActivityFriendInvite extends BaseActivity {
	private SegmentListView lsComposer;
	private SectionFriendInviteAdapter adapter;
	private List<User> users;
	private List<FriendsM2M> friendsM2Ms;
	private BaseOperationsBL baseOperationsBL;
	private View header;
	private Facebook facebook;
	private final String METHOD = "method";
	private final String FQLQUERY = "fql.query";
	private final String ACCES = "access_token";
	private final String QUERY = "query";
	private final String MESSAGE_DESC = "Come log some runs with me on Athlete.com!";
	private final String TO = "to";
	private final String MESSAGE = "message";
	private final String PARAMSforLIKERS = "select uid, name, pic from user where uid in (select uid from page_fan where uid in (select uid1 from friend where uid2=me()) and page_id in(109368782422374,121520047789,251061411860,65604112975,56874329393,8825613977,145629788018,9815486986,69770172881,136072689446)) and not is_app_user";
	private Handler mHandler = new Handler();
	private List<User> fbUser = new LinkedList<User>();
	private List<FaceBookUser> facebookUsers = new LinkedList<FaceBookUser>();
	private List<FacebookUserM2M> facebookUserM2M = new LinkedList<FacebookUserM2M>();
	private List<String> facebookUsersHideId = new LinkedList<String>();
	private AsyncFacebookRunner mAsyncRunner;
	private boolean mFBClick;
	private RelativeLayout progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_friends_invite);
		init();
		btnOnclick();
		facebookUsers = baseOperationsBL.getListFromDBBy2Field(
				FaceBookUser.class, FaceBookUser.currentUserIdField,
				getUserID(), FaceBookUser.LIKE_RUNNING, true);
		facebookUserM2M = baseOperationsBL.getListFromDBByField(
				FacebookUserM2M.class, FacebookUserM2M.CURR_USER_ID,
				getUserID());
		for (int i = 0; i < facebookUserM2M.size(); i++) {
			facebookUsersHideId.add(facebookUserM2M.get(i).getUser());
		}

		convertFbToUser();
		getFBUser();

	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityFriendInvite.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FIND_FRIEND);
	}

	private void getFBUser() {
		if (getShared().getString(Constants.SharedPreferencesKeys.FB_ACCES,
				null) != null) {
			Bundle bundle = new Bundle();
			bundle.putString(METHOD, FQLQUERY);
			bundle.putString(
					ACCES,
					getShared().getString(
							Constants.SharedPreferencesKeys.FB_ACCES, null));
			bundle.putString(QUERY, PARAMSforLIKERS);
			new AsyncFacebookRunner(facebook).request(bundle,
					new FQLRequestListener());
		}
	}

	private void convertFbToUser() {
		fbUser = new LinkedList<User>();
		if (facebookUsers != null && facebookUsers.size() > 0) {
			for (int i = 0; i < facebookUsers.size(); i++) {
				User user = new User();
				user.setId(facebookUsers.get(i).getId());
				user.setFirstName(facebookUsers.get(i).getFirstName());
				user.setProfileImage225url(facebookUsers.get(i)
						.getProfileImage225url());
				fbUser.add(user);
			}
		}

		setView();
		
		setAdapter();
	}

	private void sendFBInvite(final User user) {

		Bundle params = new Bundle();
		params.putString(MESSAGE, MESSAGE_DESC);
		params.putString(TO, user.getId().replace(Constants.FB_TAG, ""));
		params.putString(
				ACCES,
				getShared().getString(Constants.SharedPreferencesKeys.FB_ACCES,
						null));
		facebook.dialog(ActivityFriendInvite.this, "apprequests", params,
				new DialogListener() {

					@Override
					public void onFacebookError(FacebookError e) {

					}

					@Override
					public void onError(DialogError e) {

					}

					@Override
					public void onComplete(Bundle values) {
						AnalyticsUtils
								.sendPageViews(
										ActivityFriendInvite.this,
										AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FIND_FRIEND,
										AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
										AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
										AnalyticsUtils.GOOGLE_ANALYTICS.INVITE_SUGG_FB,
										0);
						baseOperationsBL.createOrUpdate(
								FacebookUserM2M.class,
								new FacebookUserM2M(user.getId().replace(
										Constants.FB_TAG, ""), getUserID(),
										true));
						baseOperationsBL.deleteBy2Field(FaceBookUser.class,
								FaceBookUser.ID, user.getId(),
								FaceBookUser.currentUserIdField, getUserID());
						users.remove(user);
						fbUser.remove(0);
						setAdapter();
					}

					@Override
					public void onCancel() {

					}
				});
	}

	private void getAllFriend() {
		showProgress();
		OnTskCpltListener getFeed = new OnTskCpltListener() {
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				List<User> pending = friendsBL
						.getFriendByUserPending(getUserID());

				if (pending != null) {
					PreferencesUtils.setString(ActivityFriendInvite.this,
							R.string.friends, String.valueOf(pending.size()));
				}
				setView();
				setAdapter();
			}
		};

		GetFriendsTask friendTask = new GetFriendsTask(
				ActivityFriendInvite.this, baseOperationsBL, getURLHost(),
				getPublicKey(), getPrivateKey(), mUserName, mApiKey,
				baseOperationsBL.getFromDBByField(User.class, User.ID,
						getUserID()), "0", null);
		getTaskManager().executeTask(friendTask, getFeed, null, true);

	}

	private void setAdapter() {
		adapter = new SectionFriendInviteAdapter(ActivityFriendInvite.this,
				users) {
			@Override
			public View getSectionView(final int position, View convertView,
					ViewGroup parent) {
				ViewHolder holder;
				if (users.size() - fbUser.size() == position + 1) {

					return header;
				} else {
					if (convertView == null) {
						holder = new ViewHolder();
						convertView = LayoutInflater.from(
								ActivityFriendInvite.this).inflate(
								R.layout.item_friend_invite, null);
						holder.txtName = (TextView) convertView
								.findViewById(R.id.txtFullName);
						holder.imAva = (RoundedImageView) convertView
								.findViewById(R.id.avaFriend);
						holder.imAva.setCornerRadius(corner2DP, size50dp);
						holder.btnAccept = (Button) convertView
								.findViewById(R.id.btnAccept);
						holder.btnCancel = (ImageButton) convertView
								.findViewById(R.id.btnCancel);
						convertView.setTag(holder);
					} else {
						holder = (ViewHolder) convertView.getTag();
						if (holder == null) {
							holder = new ViewHolder();
							convertView = LayoutInflater.from(
									ActivityFriendInvite.this).inflate(
									R.layout.item_friend_invite, null);
							holder.txtName = (TextView) convertView
									.findViewById(R.id.txtFullName);
							holder.imAva = (RoundedImageView) convertView
									.findViewById(R.id.avaFriend);
							holder.imAva.setCornerRadius(corner2DP, size50dp);
							holder.btnAccept = (Button) convertView
									.findViewById(R.id.btnAccept);
							holder.btnCancel = (ImageButton) convertView
									.findViewById(R.id.btnCancel);
							convertView.setTag(holder);
						}
					}

					final User user = getItem(position);

					if (user.getId() == null) {
						return LayoutInflater.from(ActivityFriendInvite.this)
								.inflate(R.layout.item_composer_header,
										lsComposer, false);
					}
					holder.btnCancel
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									if (user.getId().startsWith(
											Constants.FB_TAG)) {

										baseOperationsBL
												.createOrUpdate(
														FacebookUserM2M.class,
														new FacebookUserM2M(
																user.getId()
																		.replace(
																				Constants.FB_TAG,
																				""),
																getUserID(),
																true));
										baseOperationsBL.deleteBy2Field(
												FaceBookUser.class,
												FaceBookUser.ID,
												user.getId(),
												FaceBookUser.currentUserIdField,
												getUserID());
										users.remove(user);
										fbUser.remove(0);
										setAdapter();
									} else {

										cancelFriendshipRequest(user.getId());
									}

								}
							});
					if (user.getId().startsWith(Constants.FB_TAG)) {
						holder.btnAccept.setText(getString(R.string.invite));
						holder.txtName.setText(user.getFirstName());

					} else {
						holder.btnAccept.setText(getString(R.string.accept));
						holder.txtName.setText(user.getFirstName() + " "
								+ user.getLastName());
					}
					holder.btnAccept
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									if (user.getId().startsWith(
											Constants.FB_TAG)) {
										sendFBInvite(user);
									} else {

										acceptFriendshipRequest(user.getId());
									}
								}
							});
					if (user.getProfileImage225url() != null
							&& user.getProfileImage225url().startsWith("http")) {
						imageLoader.displayImage(user.getProfileImage225url(),
								holder.imAva, options);
					} else {
						holder.imAva.setImageResource(R.drawable.avatar);
					}

					holder.imAva.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View paramView) {
							if (!user.getId().startsWith(Constants.FB_TAG)) {
								startActivity(new Intent(
										ActivityFriendInvite.this,
										ActivityUserDetails.class).putExtra(
										Constants.INTENT_KEY.USER_DETAILS,
										user.getId()));
							}
						}
					});

					return convertView;
				}
			}
		};
		lsComposer.setPinnedHeaderView(LayoutInflater.from(this).inflate(
				R.layout.item_composer_header, lsComposer, false));

		lsComposer.setAdapter(adapter);
	}

	private void showProgress() {
		progressBar.setVisibility(View.VISIBLE);
		mBtnRefresh.setVisibility(View.GONE);
		splash.startAnimation(anim);
	}

	private void hideProgress() {
		mBtnRefresh.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		splash.clearAnimation();
	}

	private void btnOnclick() {
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
		findViewById(R.id.btnSearch).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AnalyticsUtils
								.sendPageViews(
										ActivityFriendInvite.this,
										AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FIND_FRIEND_SEARCH);

						startActivity(new Intent(ActivityFriendInvite.this,
								ActivitySearchUsers.class));
						startTransferAnim();
					}
				});
		mBtnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getAllFriend();
				getFBUser();
				convertFbToUser();
			}
		});
		header.findViewById(R.id.contactLinear).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(
								ActivityFriendInvite.this,
								ActivityFriendInviteContact.class),
								Constants.REQUEST_CODE_TRANSFER);
						startTransferAnim();

					}
				});
		header.findViewById(R.id.facebookLinear).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						if (getShared().getString(
								Constants.SharedPreferencesKeys.FB_ACCES, null) == null) {

							callFBAuthSavetrack();
						} else {

							startFBActivity();
						}

					}
				});

	}

	private void startFBActivity() {
		startActivityForResult(new Intent(ActivityFriendInvite.this,
				ActivityFriendInviteFacebook.class),
				Constants.REQUEST_CODE_TRANSFER);
		startTransferAnim();
	}

	private void callFBAuthSavetrack() {
		mFBClick = true;
		facebook.authorize(ActivityFriendInvite.this, Constants.PERMS,
				new LoginDialogListener());

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == Constants.REQUEST_CODE_TRANSFER
				&& resultCode == Constants.REQUEST_CODE_TRANSFER) {
			String email = data.getStringExtra(Constants.INTENT_KEY.EMAIL);
			String name = data.getStringExtra(Constants.INTENT_KEY.NAME);
			sendInvite(name, email);
		}
		if (mFBClick) {

			facebook.authorizeCallback(requestCode, resultCode, data);
			mAsyncRunner.request(Constants.FB_REQUEST_ME,
					new SampleRequestListener());
			mFBClick = false;
		}
	}

	private void cancelFriendshipRequest(String id) {
		showProgress();
		friendsM2Ms = friendsBL.getFriendM2M(getUserID(), id);
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
						if (friendsM2Ms != null && friendsM2Ms.size() > 0) {
							baseBl.delete(friendsM2Ms.get(0), FriendsM2M.class);
						}
						friendsM2Ms = null;

						List<User> pending = friendsBL
								.getFriendByUserPending(mId);

						if (pending != null) {
							PreferencesUtils.setString(
									ActivityFriendInvite.this,
									R.string.friends,
									String.valueOf(pending.size()));
						}
						setView();
						setAdapter();
					}
				} catch (Exception e) {
				}
			}
		};
		DeleteFriendshipTask deleteFriendshipTask = new DeleteFriendshipTask(
				ActivityFriendInvite.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), friendsM2Ms.get(0)
						.getFriendShipId());
		getTaskManager().executeTask(deleteFriendshipTask, deleteFriendship,
				null, true);
	}

	private void sendInvite(String name, String email) {
		showProgress();
		Toast.makeText(ActivityFriendInvite.this, getString(R.string.sending),
				Toast.LENGTH_SHORT).show();
		OnTskCpltListener sendInvite = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				hideProgress();
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.getResult()) {
						Toast.makeText(ActivityFriendInvite.this,
								getString(R.string.successful),
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
				}
			}
		};
		SendInviteByEmailTask deleteFriendshipTask = new SendInviteByEmailTask(
				ActivityFriendInvite.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), name, email);
		getTaskManager().executeTask(deleteFriendshipTask, sendInvite, null,
				true);
	}

	private void acceptFriendshipRequest(String id) {
		showProgress();
		friendsM2Ms = friendsBL.getFriendM2M(getUserID(), id);
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
						friendsM2Ms.get(0).setStatus(FriendsM2M.ACCEPTED);
						baseBl.createOrUpdate(FriendsM2M.class,
								friendsM2Ms.get(0));
						setView();
						setAdapter();
					}
				} catch (Exception e) {
				}
			}
		};
		AcceptFriendRequestTask acceptFriendRequestTask = new AcceptFriendRequestTask(
				ActivityFriendInvite.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), friendsM2Ms.get(0)
						.getFriendShipId());
		getTaskManager().executeTask(acceptFriendRequestTask,
				acceptFriendRequest, null, true);
	}

	private void init() {
		facebook = new Facebook(Constants.APP_ID_FB);

		mAsyncRunner = new AsyncFacebookRunner(facebook);
		SessionStore.restore(facebook, this);

		baseOperationsBL = new BaseOperationsBL(getHelper());
		splash = (ImageView) findViewById(R.id.imVLoading);
		mBtnRefresh = (ImageButton) findViewById(R.id.btnResresh);
		progressBar = (RelativeLayout) findViewById(R.id.relativeCircle);
		lsComposer = (SegmentListView) findViewById(R.id.lsComposer);
		friendsBL = new FriendsBL(getHelper());
		header = LayoutInflater.from(this)
				.inflate(R.layout.footer_invite, null);

	}

	private void setView() {
		users = friendsBL.getFriendByUserPending(getUserID());
		User user = new User();
		user.setId("");
		users.add(user);
		users.addAll(fbUser);
	}

	public class FQLRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {

			/*
			 * Output can be a JSONArray or a JSONObject. Try JSONArray and if
			 * there's a JSONException, parse to JSONObject
			 */
			if (response != null) {
				try {
					new JSONArray(response);

					FaceBookUserParser bookUserParser = new FaceBookUserParser();
					facebookUsers = bookUserParser.getFBUser(response, baseBl,
							getUserID(), true, facebookUsersHideId);
					setText(response);

				} catch (JSONException e) {
					try {
						/*
						 * JSONObject probably indicates there was some error
						 * Display that error, but for end user you should parse
						 * the error and show appropriate message
						 */
						new JSONObject(response);
						setText(response);
					} catch (JSONException e1) {

					}
				}
			}
		}

		public void onFacebookError(FacebookError error) {

		}
	}

	public void setText(final String txt) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {

				convertFbToUser();
			}
		});
	}

	public class SampleRequestListener implements RequestListener {
		public void onComplete(final String response, final Object state) {
			final String TOKEN = "access_token";
			final String KEY = "facebook-session";
			SharedPreferences sp = getSharedPreferences(KEY,
					Context.MODE_PRIVATE);
			final String acces_token = sp.getString(TOKEN, null);

			getShared()
					.edit()
					.putString(Constants.SharedPreferencesKeys.FB_ACCES,
							acces_token).commit();

			ActivityFriendInvite.this.runOnUiThread(new Runnable() {
				public void run() {
					startFBActivity();
				}
			});

		}

		public void onFacebookError(FacebookError e, final Object state) {

		}

		public void onFileNotFoundException(FileNotFoundException e,
				final Object state) {

		}

		public void onIOException(IOException e, final Object state) {

		}

		public void onMalformedURLException(MalformedURLException e,
				final Object state) {

		}
	}

	public final class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
		}

		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {

		}

		public void onCancel() {

		}
	}

}
