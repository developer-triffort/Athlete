package com.athlete.activity.auth;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.MainPageAdapter;
import com.athlete.control.viewpagerindicator.CirclePageIndicator;
import com.athlete.util.AnalyticsUtils;

public class ActivityAuthPagers extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private List<View> mPages;
	private View mViewLoginPager, mView2Pager, mView4Pager, mView5Pager,
			mViewShare;
	private ViewPager mPager;
	private CirclePageIndicator mCirclePageIndicator;
	private Button mBtnLogin, mBtnSignUp;
	private String pageColor = "#999999", fillColor = "#54a1c7";
	private int currentItem = 0, strokeWidth = 0;
	private String TAG_LOGIN = "tagLogin";
	private String TAG_2 = "tag2";
	private String TAG_SHARE = "tagShare";
	private String TAG_4 = "tag4";
	private String TAG_5 = "tag5";
	private final int learnMoreID = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initUi();
		AnalyticsUtils.sendPageViews(ActivityAuthPagers.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.TUTORIAL);
	}

	private void initUi() {
		LayoutInflater inflater = LayoutInflater.from(this);
		mPages = new ArrayList<View>();

		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		mBtnSignUp = (Button) findViewById(R.id.btnSignUp);

		mViewLoginPager = inflater.inflate(R.layout.actv_viewpager_login_sigup,
				null);
		mViewLoginPager.setTag(TAG_LOGIN);

		mView2Pager = inflater.inflate(R.layout.act_viewpager2, null);
		mView2Pager.setTag(TAG_2);

		mViewShare = inflater.inflate(R.layout.act_viewpager_share, null);
		mViewShare.setTag(TAG_SHARE);

		mView4Pager = inflater.inflate(R.layout.act_viewpager4, null);
		mView4Pager.setTag(TAG_4);

		mView5Pager = inflater.inflate(R.layout.act_viewpager5, null);
		mView5Pager.setTag(TAG_5);

		mPages.add(mViewLoginPager);
		mPages.add(mView2Pager);
		mPages.add(mViewShare);
		mPages.add(mView4Pager);
		mPages.add(mView5Pager);

		MainPageAdapter adapter = new MainPageAdapter(mPages);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(adapter);
		mPager.setCurrentItem(currentItem);

		mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mCirclePageIndicator.setViewPager(mPager);
		mCirclePageIndicator.setPageColor(Color.parseColor(pageColor));
		mCirclePageIndicator.setRadius(corner5DP);
		mCirclePageIndicator.setFillColor(Color.parseColor(fillColor));
		mCirclePageIndicator.setStrokeWidth(strokeWidth);
		mCirclePageIndicator.setCurrentItem(currentItem);

		mBtnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ActivityAuthPagers.this,
						ActivityLogin.class), Constants.RESULT_CODE_TAB);
				startTransferAnim();
			}
		});
		mViewLoginPager.findViewById(R.id.linearLearnMore).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPager.setCurrentItem(learnMoreID);
					}
				});
		mBtnSignUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ActivityAuthPagers.this,
						ActivitySignup.class), Constants.RESULT_CODE_TAB);
				startTransferAnim();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.RESULT_CODE_TAB) {
			finish();
		}
	}
}
