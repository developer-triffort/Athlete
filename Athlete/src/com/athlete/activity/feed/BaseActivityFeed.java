package com.athlete.activity.feed;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.track.details.BaseTrackDetails;
import com.athlete.activity.user.ActivityUserDetails;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.bl.FeedBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.control.RoundedImageView;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.model.Feed;
import com.athlete.services.IAppFilter;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

public class BaseActivityFeed extends BaseActivity {
	/**
	 * @author edBaev
	 */

	protected List<Feed> listOfFeed;
	protected BaseListAdapter<Feed> adapter;
	protected ListView listView;
	protected boolean isAllDialogsLoaded;
	protected RelativeLayout progressBar;
	protected ImageButton mBtnRefresh, mBtnSearch;
	protected ImageView splash;
	private Animation animLarge;
	// TabActivity
	protected Activity tabActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sp = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		mUserName = sp.getString(Constants.SharedPreferencesKeys.USER_NAME,
				null);
		mApiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY, null);
		mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID, null);
		tabActivity = getParent();
		progressBar = (RelativeLayout) tabActivity
				.findViewById(R.id.progressBar);
		anim = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(Constants.ANIM_DURATION);
		splash = (ImageView) tabActivity.findViewById(R.id.imVLoadingFeed);
		mBtnRefresh = (ImageButton) tabActivity.findViewById(R.id.btnResresh);

		mBtnSearch = (ImageButton) tabActivity.findViewById(R.id.btnSearch);
		// Start animating the image
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mBtnRefresh = (ImageButton) tabActivity.findViewById(R.id.btnResresh);
		isAllDialogsLoaded = false;
	}

	protected void setView() {
		feedBL = new FeedBL(getHelper());
		workoutBL = new WorkoutBL(getHelper());
		listOfFeed = new LinkedList<Feed>();
		listView = (ListView) findViewById(R.id.listView);
		updateFeed(null, true);
	}

	protected void updateFeed(String id, boolean update) {
	}

	protected void setAdapter() {

		listView.setVisibility(View.VISIBLE);
		adapter = new BaseListAdapter<Feed>(this, listOfFeed,
				R.layout.item_feed, new IAppFilter<Feed>() {

					@Override
					public boolean performFiltering(CharSequence constraint,
							Feed item) {
						return false;
					}
				}) {
			// params for paging
			@Override
			public int getCount() {
				return super.getCount()
						+ ((!isAllDialogsLoaded && !adapter.isFilter() && mBtnRefresh
								.getVisibility() == View.VISIBLE) ? 1 : 0);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final Feed item = getItem(position);
				if (position == getCount() - 1 && !isAllDialogsLoaded
						&& !adapter.isFilter()
						&& mBtnRefresh.getVisibility() == View.VISIBLE
						&& getItem(position - 1) != null) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.row_update_footer,
							null);
					view.setId(R.layout.row_update_footer);
					View splash = view.findViewById(R.id.imVLoading);
					splash.startAnimation(animLarge);

					updateFeed((String.valueOf(getItem(position - 1).getId())),
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
				} else {
					if (convertView.getTag() != null
							&& convertView.getTag() instanceof ViewHolder) {
						holder = (ViewHolder) convertView.getTag();
					} else {
						return getLayout();
					}
				}
				setVisibleInvisible(item.getBody(), holder.txtBody);
				holder.txtName.setText(item.getUser().getFirstName() + " "
						+ item.getUser().getLastName());
				// distance
				if (item.getWorkOut() != null) {
					holder.imType.setVisibility(View.VISIBLE);
					holder.txtDistance.setVisibility(View.VISIBLE);
					holder.txtTitle.setVisibility(View.VISIBLE);
					holder.txtDuration.setVisibility(View.VISIBLE);

					holder.txtDistance.setText(CommonHelper
							.convertMetersToMiles(item.getWorkOut()
									.getDistance(), isMetric())
							+ CommonHelper.getMiOrKm(isMetric())
							+ CommonHelper.getDateMSS(item.getWorkOut()
									.getDuration(), BaseActivityFeed.this));
					if (item.getWorkOut().getDuration() != 0) {
						holder.txtDuration.setText("("
								+ StringUtils.formatSpeed(
										BaseActivityFeed.this, item
												.getWorkOut().getDistance()
												/ item.getWorkOut()
														.getDuration(),
										isMetric, false) + ")");
					}

					setVisibleInvisible(item.getWorkOut().getTitle(),
							holder.txtTitle);

                    Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/athlete-regular-webfont.ttf");

                    holder.imType.setTypeface(typeFace);
                    CommonHelper.setType(holder.imType, item.getWorkOut(), null, getResources());
				} else {
					holder.txtDistance.setVisibility(View.GONE);
					holder.txtTitle.setVisibility(View.GONE);
					holder.txtDuration.setVisibility(View.GONE);
					holder.imType.setVisibility(View.GONE);

				}
				convertView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
                        if (item.getWorkOut() != null) {
							startActivity(new Intent(BaseActivityFeed.this,
									BaseTrackDetails.class).putExtra(
									Constants.INTENT_KEY.ID, item.getWorkOut().getId()));
						} else {
							startActivity(new Intent(BaseActivityFeed.this,
									BaseTrackDetails.class).putExtra(
									Constants.INTENT_KEY.FEED_ID, item.getId()));
						}
					}
				});

				if (item.getUser().getProfileImage225url().startsWith("http")) {
					imageLoader.displayImage(item.getUser()
							.getProfileImage225url(), holder.imAva, options);
				} else {
					holder.imAva.setImageResource(R.drawable.avatar);
				}
				holder.imAva.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AnalyticsUtils.sendPageViews(BaseActivityFeed.this,
								"FeedListScreen/Profile");

						startActivity(new Intent(BaseActivityFeed.this,
								ActivityUserDetails.class).putExtra(
								Constants.INTENT_KEY.USER_DETAILS, item
										.getUser().getId()));
					}
				});
				// time

				StringBuffer buffer = new StringBuffer();
				buffer.append(CommonHelper.getLastSeen(CommonHelper
						.getLongYYYYMMDDtHHMMSS(item.getDisplayDate()),
						BaseActivityFeed.this));
				// comment
				if (item.getCommentCount() > 0) {

					if (item.getCommentCount() > 1) {
						buffer.append(Constants.DOT + item.getCommentCount()
								+ " " + getString(R.string.txt_comments));
					} else {
						buffer.append(Constants.DOT + item.getCommentCount()
								+ " " + getString(R.string.txt_comment));
					}

				}
				// like
				if (item.getLikers().size() > 0) {
					if (item.getLikers().size() > 1) {
						buffer.append(Constants.DOT + item.getLikers().size()
								+ " " + getString(R.string.txt_likes));
					} else {
						buffer.append(Constants.DOT + item.getLikers().size()
								+ " " + getString(R.string.txt_like));
					}
				}
				holder.txtTime.setText(buffer.toString());
				return convertView;

			}
		};
		listView.setAdapter(adapter);
	}
}
