package com.athlete.activity.user;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.SectionComposerAdapter;
import com.athlete.control.RoundedImageView;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.get.GetFriendsTask;
import com.athlete.util.AnalyticsUtils;
import com.segment.SegmentListView;

public class ActivityFriend extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private SegmentListView lsComposer;
	private SectionComposerAdapter adapter;
	private List<User> friends;
	private User userDetails;
	private String count = "0";
	private TextView txtTitle;
	private ImageView imageLoading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_friends);
		userDetails = (User) getIntent().getSerializableExtra(
				Constants.INTENT_KEY.USER_DETAILS);
		count = (String) getIntent().getSerializableExtra(
				Constants.INTENT_KEY.USER_DETAILS_COUNT);
		friends = friendsBL.getFriendByUserAccepted(userDetails.getId());
		startTransferAnim();
		setView();
		getAllFriend();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityFriend.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FRIEND);
	}

	private void setView() {
		imageLoading = (ImageView) findViewById(R.id.imVLoading);

		txtTitle = (TextView) findViewById(R.id.txtTitle);
		mUserName = getShared().getString(
				Constants.SharedPreferencesKeys.USER_NAME, null);
		mApiKey = getShared().getString(
				Constants.SharedPreferencesKeys.API_KEY, null);
		lsComposer = (SegmentListView) findViewById(R.id.lsComposer);
		if (Integer.valueOf(count) > 1) {
			txtTitle.setText(count + " " + getString(R.string.txt_friends));
		} else {
			txtTitle.setText(count + " " + getString(R.string.txt_friend));
		}
		setAdapter();

		mBtnRefresh = (ImageButton) findViewById(R.id.btnResresh);
		mBtnRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				getAllFriend();
			}
		});
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();

			}
		});
	}

	private void setAdapter() {
		adapter = new SectionComposerAdapter(ActivityFriend.this, friends) {
			@Override
			public View getSectionView(int position, View convertView,
					ViewGroup parent) {
				ViewHolder holder;
				if (convertView == null) {
					holder = new ViewHolder();
					convertView = LayoutInflater.from(ActivityFriend.this)
							.inflate(R.layout.item_composer, null);
					holder.txtName = (TextView) convertView
							.findViewById(R.id.txtFullName);
					holder.imAva = (RoundedImageView) convertView
							.findViewById(R.id.avaFriend);
					holder.imAva.setCornerRadius(corner2DP, size50dp);

					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				final User user = getItem(position);
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View paramView) {
						startActivity(new Intent(ActivityFriend.this,
								ActivityUserDetails.class).putExtra(
								Constants.INTENT_KEY.USER_DETAILS, user.getId()));
					}
				});

				if (user.getProfileImage225url().startsWith("http")) {
					imageLoader.displayImage(user.getProfileImage225url(),
							holder.imAva, options);
				} else {
					holder.imAva.setImageResource(R.drawable.avatar);
				}

				holder.txtName.setText(user.getFirstName() + " "
						+ user.getLastName());

				return convertView;
			}
		};
		lsComposer.setPinnedHeaderView(LayoutInflater.from(this).inflate(
				R.layout.item_composer_header, lsComposer, false));
		lsComposer.setAdapter(adapter);
	}

	private void getAllFriend() {
		findViewById(R.id.relativeCircle).setVisibility(View.VISIBLE);
		mBtnRefresh.setVisibility(View.GONE);
		imageLoading.startAnimation(anim);
		OnTskCpltListener getFeed = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<ArrayList<User>> result;
				imageLoading.clearAnimation();
				mBtnRefresh.setVisibility(View.VISIBLE);
				findViewById(R.id.relativeCircle).setVisibility(View.GONE);

				try {
					result = (TaskResult<ArrayList<User>>) task.get();
					if (!result.isError() && result.getResult().size() > 0) {
						friends = friendsBL.getFriendByUserAccepted(userDetails
								.getId());
						setAdapter();
					}
				} catch (Exception e) {

				}
			}
		};

		GetFriendsTask friendTask = new GetFriendsTask(ActivityFriend.this,
				baseBl, getURLHost(), getPublicKey(), getPrivateKey(),
				mUserName, mApiKey, userDetails, count, null);
		getTaskManager().executeTask(friendTask, getFeed, null, true);

	}
}
