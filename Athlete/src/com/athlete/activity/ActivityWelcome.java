package com.athlete.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.auth.ActivityAuthPagers;

public class ActivityWelcome extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private static final int SPLASH_TIME = 300;
	protected boolean isActive = true;
	private AsyncTask<Boolean, Boolean, Boolean> splashTask;
	private int hungred = 100;
	private String userName;
	private String apiKey;
	private SharedPreferences sp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.actv_welcome);
		
		final ImageView splash = (ImageView) findViewById(R.id.imageRotate);
		Animation anim = AnimationUtils.loadAnimation(this,
				R.anim.anim_progressbar);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(Constants.ANIM_DURATION);
		// Start animating the image
		splash.startAnimation(anim);

		splashTask = new AsyncTask<Boolean, Boolean, Boolean>() {
			@Override
			protected Boolean doInBackground(Boolean... params) {
				sp = getSharedPreferences(Constants.PREFERENCES,
						Context.MODE_PRIVATE);
				userName = sp.getString(
						Constants.SharedPreferencesKeys.USER_NAME, null);
				apiKey = sp.getString(Constants.SharedPreferencesKeys.API_KEY,
						null);
				mId = sp.getString(Constants.SharedPreferencesKeys.CURRENT_ID,
						null);

				try {
					int waited = 0;
					while (isActive && (waited < SPLASH_TIME)) {
						Thread.sleep(Constants.ONE_SECOND);
						if (isActive) {
							waited += hungred;
						}
					}
				} catch (InterruptedException e) {
				}
				return true;

			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);

				startApp();

			}
		};
		splashTask.execute((Boolean[]) null);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		splashTask.cancel(true);
	}

	private void startApp() {
		if (userName == null || apiKey == null) {
			startActivity(new Intent(ActivityWelcome.this,
					ActivityAuthPagers.class));
			finish();
		} else {

			startActivity(new Intent(ActivityWelcome.this,
					TabActivityMain.class));
			finish();

		}
		transitionType = TransitionType.Zoom;
		overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
	}
}
