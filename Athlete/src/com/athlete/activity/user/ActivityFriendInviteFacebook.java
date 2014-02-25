package com.athlete.activity.user;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.auth.ActivityBaseAuth;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.bl.BaseBl;
import com.athlete.control.RoundedImageView;
import com.athlete.model.FaceBookUser;
import com.athlete.model.FacebookUserM2M;
import com.athlete.parser.FaceBookUserParser;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.BaseRequestListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class ActivityFriendInviteFacebook extends ActivityBaseAuth {
	private ListView mListView;
	private final String METHOD = "method";
	private final String FQLQUERY = "fql.query";
	private final String ACCES = "access_token";
	private final String QUERY = "query";
	private final String PARAMS_ALL_FRIENDS = "SELECT uid, name, pic, is_app_user FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) and is_app_user = 0";
	private Facebook facebook;
	private List<FaceBookUser> facebookUsers = new LinkedList<FaceBookUser>();
	private Handler mHandler = new Handler();
	private BaseListAdapter<FaceBookUser> adapter;
	private final String MESSAGE_DESC = "Come log some runs with me on Athlete.com!";
	private final String TO = "to";
	private final String MESSAGE = "message";
	private List<FacebookUserM2M> facebookUserM2M = new LinkedList<FacebookUserM2M>();
	private List<String> facebookUsersHideId = new LinkedList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_contact);
		mListView = (ListView) findViewById(R.id.listViewContact);
		((TextView) findViewById(R.id.txtTitle))
				.setText(getString(R.string.footer_fb_friends));
		facebook = new Facebook(Constants.APP_ID_FB);
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
		baseBl = new BaseBl(ActivityFriendInviteFacebook.this);
		facebookUsers = baseBl.getListFromDBBy2Field(FaceBookUser.class,
				FaceBookUser.currentUserIdField, getUserID(),
				FaceBookUser.LIKE_RUNNING, false);

		facebookUserM2M = baseBl.getListFromDBByField(FacebookUserM2M.class,
				FacebookUserM2M.CURR_USER_ID, getUserID());
		for (int i = 0; i < facebookUserM2M.size(); i++) {
			facebookUsersHideId.add(facebookUserM2M.get(i).getUser());
		}

		setAdapter();
		getFBUser();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityFriendInviteFacebook.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FB_FRIEND);
	}

	private void sendFBInvite(final FaceBookUser fbUser) {
		Bundle params = new Bundle();
		params.putString(MESSAGE, MESSAGE_DESC);
		params.putString(TO, fbUser.getId());
		params.putString(
				ACCES,
				getShared().getString(Constants.SharedPreferencesKeys.FB_ACCES,
						null));
		facebook.dialog(ActivityFriendInviteFacebook.this, "apprequests",
				params, new DialogListener() {

					@Override
					public void onFacebookError(FacebookError e) {

					}

					@Override
					public void onError(DialogError e) {

					}

					@Override
					public void onComplete(Bundle values) {

						deleteItem(fbUser);
					}

					@Override
					public void onCancel() {

					}
				});
	}

	private void deleteItem(FaceBookUser fbUser) {

		baseBl.createOrUpdate(FacebookUserM2M.class,
				new FacebookUserM2M(fbUser.getId(), getUserID(), true));
		baseBl.deleteBy2Field(FaceBookUser.class, FaceBookUser.ID,
				fbUser.getId(), FaceBookUser.currentUserIdField, getUserID());
		facebookUsers.remove(fbUser);
		setAdapter();
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<FaceBookUser>(
				ActivityFriendInviteFacebook.this, facebookUsers,
				R.layout.item_facebook) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final FaceBookUser user = getItem(position);

				if (convertView == null) {
					holder = new ViewHolder();
					convertView = getLayout();
					holder.txtName = (TextView) convertView
							.findViewById(R.id.txtFullName);
					holder.txtArea = (TextView) convertView
							.findViewById(R.id.txtArea);
					holder.imAva = (RoundedImageView) convertView
							.findViewById(R.id.avaFriend);
					holder.imAva.setCornerRadius(corner2DP, size50dp);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				if (user.getProfileImage225url().startsWith("http")) {
					imageLoader.displayImage(user.getProfileImage225url(),
							holder.imAva, options);
				} else {
					holder.imAva.setImageResource(R.drawable.avatar);
				}
				holder.txtName.setText(user.getFirstName());
				convertView.findViewById(R.id.btnInvite).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								AnalyticsUtils
										.sendPageViews(
												ActivityFriendInviteFacebook.this,
												AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FB_FRIEND,
												AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
												AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
												AnalyticsUtils.GOOGLE_ANALYTICS.INVITE_FB,
												0);

								sendFBInvite(user);

							}
						});
				convertView.findViewById(R.id.btnCancel).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {

								deleteItem(user);
							}
						});
				return convertView;

			}
		};
		adapter.setNotifyOnChange(true);
		mListView.setAdapter(adapter);
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
			bundle.putString(QUERY, PARAMS_ALL_FRIENDS);
			new AsyncFacebookRunner(facebook).request(bundle,
					new BaseRequestListener() {

						@Override
						public void onComplete(String response, Object state) {
							FaceBookUserParser bookUserParser = new FaceBookUserParser();
							facebookUsers = bookUserParser.getFBUser(response,
									baseBl, getUserID(), false,
									facebookUsersHideId);
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									setAdapter();
								}
							});
						}
					});
		}
	}

}
