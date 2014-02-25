package com.athlete.activity.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.LoginTask;
import com.athlete.util.AnalyticsUtils;

public class ActivityLogin extends ActivityBaseAuth implements
		OnTskCpltListener {
	/**
	 * @author edBaev
	 */
	private EditText mEdTxtEmail, mEdTxtPass;
	private Button mBtnLogIn, mBtnLogInFB, mBtnPass;
	private final String emailAdmin = "admin";
	private final String passAdmin = "athlete";
	private TextView txtServer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_login);
		setView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityLogin.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.LOGIN);
	}

	private void setView() {
		mEdTxtEmail = (EditText) findViewById(R.id.edTxtEmail);
		mEdTxtPass = (EditText) findViewById(R.id.edTxtPassword);
		mBtnLogIn = (Button) findViewById(R.id.btnLogIn);
		mBtnLogInFB = (Button) findViewById(R.id.btnLoginWithFB);
		txtServer = (TextView) findViewById(R.id.txtServer);
		mBtnLogInFB
				.setText(Html.fromHtml("<b>Log in </b>with <b>facebook</b>"));
		mBtnPass = (Button) findViewById(R.id.btnPass);
		String urlhost = ((AthleteApplication) getApplication()).getUrlHost();
		if (urlhost.equals(Constants.HOST.HOST_MAIN)) {
			txtServer.setVisibility(View.GONE);
		} else {
			txtServer.setVisibility(View.VISIBLE);
			txtServer.setText(urlhost);
		}
		mBtnLogIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				hideKeyboard(ActivityLogin.this);
				if (mEdTxtEmail.getText().toString()
						.equalsIgnoreCase(emailAdmin)
						&& mEdTxtPass.getText().toString()
								.equalsIgnoreCase(passAdmin)) {
					setAdapterForDialog();

				} else {
					AnalyticsUtils.sendPageViews(ActivityLogin.this,
							AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.LOGIN,
							AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
							AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
							AnalyticsUtils.GOOGLE_ANALYTICS.LOGIN_EMAIL, 0);
					LoginTask task = new LoginTask(ActivityLogin.this,
							getURLHost(), getPublicKey(), getPrivateKey(),
							mEdTxtEmail.getText().toString(), mEdTxtPass
									.getText().toString());
					getTaskManager().executeTask(task, ActivityLogin.this,
							null, true);
				}
			}
		});
		mBtnLogInFB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideKeyboard(ActivityLogin.this);
				mFBClick = true;
				mIsLogin = true;
				isSaveTrack = false;

				mFacebook.authorize(ActivityLogin.this, Constants.PERMS,
						new LoginDialogListener());
				AnalyticsUtils.sendPageViews(ActivityLogin.this,
						AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.LOGIN,
						AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
						AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
						AnalyticsUtils.GOOGLE_ANALYTICS.LOGIN_FB, 0);
			}
		});
		findViewById(R.id.btnSignUp).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(ActivityLogin.this,
								ActivitySignup.class),
								Constants.RESULT_CODE_TAB);
						startTransferAnim();
					}

				});
		mBtnPass.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(Constants.URL_FORGOT_PASS));
				startActivity(browserIntent);

			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		startTransferAnim();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onTaskComplete(BaseTask task) {
		TaskResult<String[]> result;
		try {
			result = (TaskResult<String[]>) task.get();
			if (result.isError()) {
				String toast;
				if (result.getError_description() != null
						&& result.getError_description().length() > 0) {
					toast = result.getError_description();
				} else {
					toast = getString(R.string.toast_non_internet);
				}
				Toast.makeText(ActivityLogin.this, toast, Toast.LENGTH_SHORT)
						.show();

			} else {

				SharedPreferences sp = getSharedPreferences(
						Constants.PREFERENCES, Context.MODE_PRIVATE);
				sp.edit()
						.putString(Constants.SharedPreferencesKeys.USER_NAME,
								result.getResult()[0]).commit();
				sp.edit()
						.putString(Constants.SharedPreferencesKeys.API_KEY,
								result.getResult()[1]).commit();

				meTaskLogin(result.getResult()[0], result.getResult()[1]);

			}

		} catch (Exception e) {
		}

	}

	protected void setAdapterForDialog() {
		deleteAllDatabseTable();

		final String[] items = { Constants.HOST.HOST_MAIN,
				Constants.HOST.HOST_STAGING, Constants.HOST.HOST_TEST
		};
		ListAdapter adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, android.R.id.text1, items) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				return v;
			}
		};

		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.choose_server))
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						((AthleteApplication) getApplication())
								.setUrlHost(items[item].toString());
						getShared().edit().putString(
								Constants.SharedPreferencesKeys.SERVER,
								items[item].toString());
						txtServer.setVisibility(View.VISIBLE);
						txtServer.setText(items[item].toString());
						mEdTxtEmail.setText("");
						mEdTxtPass.setText("");
					}
				}).show();
	}
}
