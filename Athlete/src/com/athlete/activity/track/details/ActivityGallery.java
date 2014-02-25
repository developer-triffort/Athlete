package com.athlete.activity.track.details;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.MainPageAdapter;
import com.athlete.control.PinchImageView;
import com.athlete.control.viewpagerindicator.CirclePageIndicator;
import com.athlete.model.PostPicture;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ActivityGallery extends BaseActivity {

	private List<View> mPages;

	private ViewPager mPager;
	private CirclePageIndicator mCirclePageIndicator;

	private final String pageColor = "#999999", fillColor = "#54a1c7";
	private int currentItem, strokeWidth = 0;

	private List<PostPicture> postPictures;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_gallery);
		initUi();
	}

	private void initUi() {

		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
		int feedId = getIntent().getIntExtra(Constants.INTENT_KEY.FEED_ID, -1);
		currentItem = getIntent().getIntExtra(Constants.INTENT_KEY.ID, 0);
		if (feedId == -1) {
			finish();
		} else {
			postPictures = workoutBL.getListFromDBByField(PostPicture.class,
					PostPicture.FEED_ID, feedId);
			setPictureList();
		}

	}

	private void setPictureList() {
		mPages = new ArrayList<View>();
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.removeAllViews();
		if (postPictures != null && !postPictures.isEmpty()) {
			for (final PostPicture postPicture : postPictures) {
				final View view = getLayoutInflater().inflate(
						R.layout.item_gallery, null);
				PinchImageView imageView = (PinchImageView) view
						.findViewById(R.id.imViewPicture);
				String url;
				if (postPicture.getDetail() == null) {
					url = postPicture.getOriginal();
				} else {
					url = postPicture.getDetail();
				}
				imageLoader.displayImage(url, imageView,
						new ImageLoadingListener() {
							@Override
							public void onLoadingStarted() {
								view.findViewById(R.id.progressBar)
										.setVisibility(View.VISIBLE);
							}

							@Override
							public void onLoadingFailed(FailReason arg0) {
							}

							@Override
							public void onLoadingComplete() {
								view.findViewById(R.id.progressBar)
										.setVisibility(View.GONE);
							}

							@Override
							public void onLoadingCancelled() {

							}
						});
				mPages.add(view);
			}
		}
		MainPageAdapter adapter = new MainPageAdapter(mPages);
		mPager.setCurrentItem(currentItem);
		mPager.setAdapter(adapter);
		mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mCirclePageIndicator.setViewPager(mPager);
		mCirclePageIndicator.setPageColor(Color.parseColor(pageColor));
		mCirclePageIndicator.setRadius(corner5DP);
		mCirclePageIndicator.setFillColor(Color.parseColor(fillColor));
		mCirclePageIndicator.setStrokeWidth(strokeWidth);
		mCirclePageIndicator.setCurrentItem(currentItem);

	}
}